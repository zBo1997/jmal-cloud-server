#!/bin/bash
# version
if [ -n "$1" ]; then
  echo "version: $1"
else
  echo "需要带上版本号, 例如 sh update.sh 2.1.8"
  exit
fi
version=$1
# jmal-cloud-view Directory location
view_dir="/Users/jmal/studio/myProject/github/jmal-cloud-view"
# jmal-cloud-server Directory location
server_dir="/Users/jmal/studio/myProject/github/jmal-cloud-server"
# docker aliyun registry password

is_arm() {
  # shellcheck disable=SC2155
  local get_arch=$(arch)
  if [[ $get_arch =~ "aarch" ]] || [[ $get_arch =~ "arm" ]]; then
    echo "yes"
  else
    echo "no"
  fi
}

# build jmal-cloud-view
cd $view_dir || exit
echo "location: ${view_dir} "
if [ ! -f "dist-$version.tar" ]; then
  npm run build:prod
  tar -czf "dist-$version.tar" dist
fi
echo "current $(pwd)"
echo "copy dist-$version.tar to $server_dir/docker/"
cp "dist-$version.tar" $server_dir"/docker/"
echo "copy dist-$version.tar to $server_dir/www/releases/dist-latest.tar"
cp "dist-$version.tar" $server_dir"/www/releases/dist-latest.tar"

# build jmal-cloud-server
cd $server_dir || exit
echo "current $(pwd)"
echo "location: ${server_dir} "
if [ ! -f "target/clouddisk-$version-exec.jar" ]; then
  mvn clean
  mvn -DskipTests=true package
fi
cp "target/clouddisk-$version-exec.jar" "$server_dir/docker/"
echo "copy target/clouddisk-$version-exec.jar $server_dir/docker/"

# build jmalcloud of Dockerfile
cd "$server_dir/docker" || exit
echo "location: ${server_dir}/docker "
docker build -t "jmalcloud:$version" --build-arg "version=$version" . --load
docker tag "jmalcloud:$version" "jmalcloud:latest"

docker_arch=""

if [[ "$(is_arm)" == "yes" ]]; then
    docker_arch="-arm64"
else
    docker_arch=""
fi

pushAliYun() {
  echo "Push the image to the $1 ..."
  # shellcheck disable=SC2002
  cat pwd.txt | docker login --username=bjmal --password-stdin "$1"
  docker tag "jmalcloud:$version" "$1/jmalcloud/jmalcloud:$version$docker_arch"
  docker tag "jmalcloud:$version" "$1/jmalcloud/jmalcloud:latest$docker_arch"
  docker push "$1/jmalcloud/jmalcloud:$version$docker_arch"
  docker push "$1/jmalcloud/jmalcloud:latest$docker_arch"
  removeLocalAliYunTag "$1"
}

pushDockerHub() {
  cat docker_hub_pwd.txt | docker login --username=jmal --password-stdin
  echo "Push the image to the DockerHub ..."
  docker buildx build --platform linux/amd64,linux/arm64 -t "jmal/jmalcloud:$version" --build-arg "version=$version" . --push
  docker buildx build --platform linux/amd64,linux/arm64 -t "jmal/jmalcloud:latest" --build-arg "version=$version" . --push
}

removeLocalAliYunTag() {
  docker rmi "$1/jmalcloud/jmalcloud:$version$docker_arch"
  docker rmi "$1/jmalcloud/jmalcloud:latest$docker_arch"
  echo "removed the image $1"
}

# Push the image to the registry
pushDockerHub
pushAliYun "registry.cn-guangzhou.aliyuncs.com"
#pushAliYun "registry.cn-hangzhou.aliyuncs.com"
#pushAliYun "registry.cn-chengdu.aliyuncs.com"
#pushAliYun "registry.cn-beijing.aliyuncs.com"
exit 0
