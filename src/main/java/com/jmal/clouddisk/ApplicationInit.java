package com.jmal.clouddisk;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.io.FileUtil;
import com.jmal.clouddisk.config.FileProperties;
import com.jmal.clouddisk.listener.FileListener;
import com.jmal.clouddisk.listener.TempDirFilter;
import com.jmal.clouddisk.service.MongodbIndex;
import com.jmal.clouddisk.util.CaffeineUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @Description 启动时开启文件目录监控
 * @Date 2020-02-21 14:54
 * @author jmal
 */
@Component
@Slf4j
public class ApplicationInit implements ApplicationRunner {

    final
    FileProperties fileProperties;

    final
    FileListener fileListener;

    final
    MongodbIndex mongodbIndex;

    private FileAlterationMonitor monitor;

    private FileAlterationObserver observer;

    private boolean isMonitor = true;

    public ApplicationInit(FileProperties fileProperties, FileListener fileListener, MongodbIndex mongodbIndex) {
        this.fileProperties = fileProperties;
        this.fileListener = fileListener;
        this.mongodbIndex = mongodbIndex;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 判断是否开启文件监控
        if (Boolean.FALSE.equals(fileProperties.getMonitor())) {
            return;
        }
        Path rootDir = Paths.get(fileProperties.getRootDir());
        if (!Files.exists(rootDir)) {
            Files.createDirectories(rootDir);
        }
        // 轮询间隔(秒)
        long interval = TimeUnit.SECONDS.toMillis(fileProperties.getTimeInterval());
        // 创建过滤器
        TempDirFilter tempDirFilter = new TempDirFilter(fileProperties.getRootDir(), fileProperties.getChunkFileDir());

        // 使用过滤器
        observer = new FileAlterationObserver(new File(fileProperties.getRootDir()), tempDirFilter);
        // 不使用过滤器
        observer.addListener(fileListener);
        //创建文件变化监听器
        monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        monitor.start();
        log.info("\r\n文件监控服务已开启:\r\n轮询间隔:{}秒\n监控目录:{}\n忽略目录:{}", fileProperties.getTimeInterval(), rootDir, rootDir + File.separator + fileProperties.getChunkFileDir());
        // 检测mongo索引
        mongodbIndex.checkMongoIndex();
    }

    /**
     * 5分钟没人访问时，降低轮询频率
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 5000)
    private void check() {
        long diff = System.currentTimeMillis() - CaffeineUtil.getLastAccessTimeCache();
        try {
            if (diff > DateUnit.MINUTE.getMillis() * 5) {
                if (isMonitor) {
                    monitor.stop(DateUnit.SECOND.getMillis());
                    monitor = null;
                    monitor = new FileAlterationMonitor(DateUnit.MINUTE.getMillis() * 30, observer);
                    monitor.start();
                    log.info("轮询间隔改为30分钟");
                    isMonitor = false;
                }
            } else {
                if (!isMonitor) {
                    monitor.stop(DateUnit.SECOND.getMillis());
                    monitor = null;
                    monitor = new FileAlterationMonitor(DateUnit.SECOND.getMillis() * 3, observer);
                    monitor.start();
                    log.info("轮询间隔改为3秒钟");
                    isMonitor = true;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 定时清理临时目录
     * 每天凌晨2点执行
     * 清理临时目录中7天前的文件
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void cleanTempDir() {
        // 临时目录
        Path tempPath = Paths.get(fileProperties.getRootDir(), fileProperties.getChunkFileDir());
        for (File username : FileUtil.ls(tempPath.toString())) {
            if (username.isDirectory()) {
                for (File file : FileUtil.ls(username.getAbsolutePath())) {
                    if (file.lastModified() < (System.currentTimeMillis() - DateUnit.DAY.getMillis() * 7)) {
                        FileUtil.del(file);
                    }
                }
            }
        }
    }
}
