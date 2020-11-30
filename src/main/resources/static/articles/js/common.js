$(function() {
    /***
     * 动态打字效果
     */
    let divTyping = $('#article-title')
    let i = 0,timer = 0,str = divTyping.text()
    function typing () {
        if (i <= str.length) {
            divTyping.text(str.slice(0, i++))
            timer = setTimeout(typing, 100)
        } else {
            clearTimeout(timer)
        }
    }
    typing()
    let labelIcon = $('.changeTheme i')
    let labelTitle = $('.changeTheme span')
    // 添加暗色主题
    function addDarkTheme() {
        const themeDark = $('#theme-css-dark')
        if(themeDark.length > 0){
            return
        }
        let link = document.createElement('link');
        link.type = 'text/css';
        link.id = "theme-css-dark";  // 加上id方便后面好查找到进行删除
        link.rel = 'stylesheet';
        link.href = '/articles/css/dark/index.css';
        document.getElementsByTagName("head")[0].appendChild(link);
        labelIcon.removeClass('fa-moon')
        labelIcon.addClass('fa-sun')
        labelTitle.html('亮色')
    }
    // 删除暗色主题
    function removeDarkTheme() {
        const themeDark = $('#theme-css-dark')
        if(themeDark.length > 0){
            themeDark.remove();
            labelIcon.removeClass('fa-sun')
            labelIcon.addClass('fa-moon')
            labelTitle.html('暗色')
        }
    }
    // 使用暗色主题(记录选择到cookie中)
    function useDarkTheme(useDark) {
        setCookie('jmal-theme', useDark ? "dark" : "light");
        if (useDark) {
            addDarkTheme();
        } else {
            removeDarkTheme();
        }
    }
    // 获取cookie中选中的主题名称，没有就给个默认的
    function getThemeCSSName() {
        return getCookie('jmal-theme') || "light";
    }
    $('.changeTheme').click(function (){
        useDarkTheme(getThemeCSSName() === 'light')
    })

    let media = window.matchMedia('(prefers-color-scheme: dark)');
    let callback = (e) => {
        if (e.matches) {
            useDarkTheme(true)
        } else {
            useDarkTheme(false)
        }
    };
    if (typeof media.addEventListener === 'function') {
        media.addEventListener('change', callback);
    } else if (typeof media.addListener === 'function') {
        media.addListener(callback);
    }

    function setCookie(name, value) {
        document.cookie = name + "=" + value + ";path=/";
    }

    function getCookie(cname) {
        let name = cname + "=";
        let decodedCookie = decodeURIComponent(document.cookie);
        let ca = decodedCookie.split(';');
        for(let i = 0; i <ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) === 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    }
})