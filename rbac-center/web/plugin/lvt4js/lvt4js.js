/**
 * 各种js小工具<br>
 * 加入了各种jQuery扩展
 * Author ： LV
 * require : jquery 1.6+
 */
var LVT = {};
if(!T) var T = LVT;
LVT.mUrl = function(){
    var js = document.scripts;
    var url =js[js.length - 1].src;
    return url.substring(0, url.lastIndexOf('/'));
}();
/**
 * checkbox联动效果实现
 * 主checkbox添加forgroup参数 子checkbox添加group参数
 * 需要在页面载入完成后调用LVT.cbxRely.init()
 * 页面动态生成子checkbox后也需要调用LVT.cbxRely.init()
 */
LVT.cbxRely = {
    changeSub:function(){
        var groupName = $(this).attr('group');
        $(":checkbox[forgroup="+groupName+"]").prop('checked',
                $(":checkbox[group="+groupName+"]").length==$(":checked[group="+groupName+"]").length);
    },
    changeGroup:function(){
        $(":checkbox[group="+$(this).attr('forgroup')+"]").prop('checked',$(this).prop('checked')).change();
    },
    init:function(){
        $(':checkbox[group]').unbind('change',LVT.cbxRely.changeSub).bind('change',LVT.cbxRely.changeSub);
        $(':checkbox[forgroup]').unbind('change',LVT.cbxRely.changeSub).bind('change',LVT.cbxRely.changeGroup);
        $(":checkbox[forgroup]").each(function(){
            var groupName = $(this).attr('forgroup');
            $(":checkbox[forgroup="+groupName+"]").prop('checked',
                    $(":checkbox[group="+groupName+"]").length!=0 &&
                    $(":checkbox[group="+groupName+"]").length==$(":checked[group="+groupName+"]").length);
        })
    }
};
if(!TcbxRely) var TcbxRely=LVT.cbxRely;
if(!$cbxRely) var $cbxRely=LVT.cbxRely;

/**
 * 数组相关功能
 * @param arr
 * @param ele
 * @returns {Boolean}
 */
LVT.arr = {
    /**
     * 数组是否包含某元素
     * @param arr
     * @param ele
     * @returns {Boolean}
     */
    contains : function(arr, ele){
        try{
            for(var c = 0; c < arr.length; c++) 
                if(arr[c]==ele) return true;
        }catch(ignore){}
        return false;
    },
    /**
     * 向数组添加一个元素，若已有该元素则忽略
     * @param arr
     * @param ele
     * @returns 添加元素后的数组
     */
    add : function(arr, ele){
        var tmpArr = [];
        var contain = false;
        for(var c = 0; c < arr.length; c++){
            if(!contain) contain = arr[c]==ele;
            tmpArr.push(arr[c]);
        }
        if(contain) return tmpArr;
        tmpArr.push(ele);
        return tmpArr;
    },
    /**
     * 从数组删除一个元素，若没有该元素则忽略
     * @param arr
     * @param ele
     * @returns 删除元素后的数组
     */
    remove : function(arr, ele){
        var tmpArr = [];
        for(var c = 0; c < arr.length; c++){
            if(arr[c]!=ele) tmpArr.push(arr[c]);
        }
        return tmpArr;
    },
    /**
     * 克隆一个数组
     * @param arr
     */
    clone : function(arr){
        var tmpArr = [];
        for(var c = 0; c < arr.length; c++) tmpArr.push(arr[c]);
        return tmpArr;
    }
};
if(!Tarr) var Tarr=LVT.arr;
if(!$arr) var $arr=LVT.arr;

LVT.uuid = function(factor){
    var d = new Date().getTime();
    if(factor!=null) d += LVT.obj.hashCode(factor);
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c){
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
    });
    return uuid;
};
if(!Tuuid) var Tuuid=LVT.uuid;
if(!jQuery.uuid) jQuery.uuid=LVT.uuid;

/** 字符串相关工具 */
LVT.str = {
    /** 同java字符串的startsWith函数 */
    startsWith : function(str,prefix){
        return str.substring(0,prefix.length)==prefix;
    },
    /** 同java字符串的endsWith函数 */
    endsWith : function(str,suffix){
        return str.substring(str.length-suffix.length,str.length)==suffix;
    },
    /** 避免html字符 */
    htmlEscape : function(str){
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }
};
if(!Tstr) var Tstr=LVT.str;
if(!$str) var $str=LVT.str;

/**
 * 弹出一个浮在最上方,遮住全部的loading图案<br>
 * 采用延迟机制，若调用LVT.loader.show()的LVT.loader.threshold(默认300)ms后，没有调用LVT.loader.hide(),才出现
 */
LVT.loader = {
    container : null,
    msger : null,
    msgs : [],
    threshold : 300,
    gif : LVT.mUrl+'/img/loader.gif',
    init : function(){
        if(LVT.loader.container) return;
        LVT.loader.container = jQuery('<div style="position:fixed;width:100%;height:100%;background-color:rgba(0,0,0,0.23);z-index:20000;display:none;">'+
            '<div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;"><div>'+
            '<div style="text-align:center;"><img src="'+LVT.loader.gif+'"></div>'+
            '<div class="lvt-loader-msger" style="text-align:center;color:white;font-weight:bolder;font-size:xx-large;"></div>'+
        '</div></div></div>');
        LVT.loader.msger = LVT.loader.container.find('.lvt-loader-msger');
        $('body').prepend(LVT.loader.container);
    },
    show : function(msg){
        if(msg==null) return;
        LVT.loader.init();
        var loadingMsgObj = LVT.loader.loadingMsgObj(msg);
        LVT.loader.msgs.push(loadingMsgObj);
        LVT.loader.msger.append(loadingMsgObj.ele);
        setTimeout('LVT.loader.internalShow()', LVT.loader.threshold);
    },
    immediateShow : function(msg){
        if(msg==null) return;
        LVT.loader.init();
        var loadingMsgObj = LVT.loader.loadingMsgObj(msg);
        LVT.loader.msgs.push(loadingMsgObj);
        LVT.loader.msger.append(loadingMsgObj.ele);
        LVT.loader.internalShow();
    },
    loadingMsgObj : function(msg){
        var loadingMsgHash = LVT.obj.hashCode(msg);
        var loadingMsgEle = jQuery('<p></p>');
        loadingMsgEle.html(msg);
        return {msg:msg, hash:loadingMsgHash, ele:loadingMsgEle};
    },
    internalShow : function(){
        if(LVT.loader.msgs.length==0) return;
        LVT.loader.container.show();
    },
    hide : function(msg){
        if(msg==null) return;
        var tmpMsgObj = LVT.loader.loadingMsgObj(msg);
        var loadingMsgObj;
        for(var i=0; i<LVT.loader.msgs.length; i++){
            var msgObj = LVT.loader.msgs[i];
            if(msgObj.hash!=tmpMsgObj.hash) continue;
            loadingMsgObj = msgObj;
            break;
        }
        if(loadingMsgObj==null) return;
        loadingMsgObj.ele.remove();
        LVT.loader.msgs = LVT.arr.remove(LVT.loader.msgs, loadingMsgObj);
        if(LVT.loader.msgs.length==0) LVT.loader.container.hide();
    },
    change : function(origMsg, msg){
        if(origMsg==null && msg==null) return;
        if(origMsg==null) return LVT.loader.immediateShow(msg);
        if(msg==null) return LVT.loader.hide(origMsg);
        var tmpMsgObj = LVT.loader.loadingMsgObj(origMsg);
        var loadingMsgObj;
        for(var i=0; i<LVT.loader.msgs.length; i++){
            var msgObj = LVT.loader.msgs[i];
            if(msgObj.hash!=tmpMsgObj.hash) continue;
            loadingMsgObj = msgObj;
            break;
        }
        if(loadingMsgObj==null) return LVT.loader.immediateShow(msg);
        tmpMsgObj = LVT.loader.loadingMsgObj(msg);
        loadingMsgObj.hash = tmpMsgObj.hash;
        loadingMsgObj.ele.html(msg);
    }
};
if(!Tloader) var Tloader=LVT.loader;
if(!$loader) var $loader=LVT.loader;

/** 弹出一个浮动条显示信息，一段时间后自动隐藏 */
LVT.toast = {
    container : null,
    threshold : 1500,
    init : function(){
        if(LVT.toast.container) return;
        LVT.toast.container = jQuery('<div style="position:fixed;width:100%;height:100%;z-index:20000;display:none;">'
            +'<div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;">'
            +'<div style="background-color:rgba(0,0,0,0.58);width:30%;min-height:30%;border-radius:20px;display:flex;align-items:center;justify-content:center;">'
            +'<div class="lvt-toast-msger" style="word-break:break-all;word-wrap:break-word;text-align:center;vertical-align:middle;color:white;font-weight:bolder;font-size:xx-large;"></div>'
        +'</div></div></div>');
        LVT.toast.msger = LVT.toast.container.find('.lvt-toast-msger');
        $('body').prepend(LVT.toast.container);
    },
    show : function(msg){
        if(!msg) return;
        LVT.toast.init();
        LVT.toast.msger.text(msg);
        LVT.toast.container.fadeIn(LVT.toast.threshold).fadeOut(LVT.toast.threshold);
    }
};
if(!Ttoast) var Ttoast=LVT.toast;
if(!$toast) var $toast=LVT.toast;

LVT.params = function(url){
    if(url==null) url=location.href;
    var search = location.search;
    if(!search) return {};
    search = search.substring(1, search.length);
    if(!search) return {};
    search = search.split('&');
    var params = {};
    for(var i=0; i<search.length; i++){
        var pair = search[i];
        var ePos = pair.indexOf('=');
        if(ePos<=-1) ePos=pair.length;
        var key = pair.substring(0, ePos);
        if(!key) continue;
        params[key] = decodeURIComponent(pair.substring(ePos+1, pair.length));
    }
    return params;
}
if(!Tparams) var Tparams=LVT.params;
if(!jQuery.params) jQuery.params=LVT.params;

/**
 * 回避null,undefined,NaN,输出空字符串
 */
LVT.ig = function(val){
    if(val==null || val==undefined) return '';
    if(typeof val =='number' && (isNaN(val) || !isFinite(val))) return '';
    return val;
};
if(!Tig) var Tig=LVT.ig;
if(!jQuery.ig) jQuery.ig=LVT.ig;

/**
 * 回避null,undefined,NaN,输出空字符串
 * 转义html保留字符等
 */
LVT.igh = function(val){
    val = LVT.ig(val);
    if(typeof val != 'string') val = JSON.stringify(val);
    return LVT.str.htmlEscape(val);
};
if(!Tigh) var Tigh=LVT.igh;
if(!jQuery.igh) jQuery.igh=LVT.igh;

/**
 * obj相关功能
 */
LVT.obj = {
    /** 判断一个object是否为空 */
    isEmpty : function(obj){
        for(var key in obj){
            return false;
        }
        return true;
    },
    /** 计算object中键值对的个数 */
    size : function(obj){
        var size = 0;
        for(var key in obj) size++;
        return size;
    },
    /** 克隆一个object */
    clone : function(obj){
        var tmp = {};
        for(var key in obj){
            tmp[key] = obj[key];
        }
        return tmp;
    },
    /** 从object删除指定key */
    remove : function(obj, key2Del){
        var tmp = {};
        for(var key in obj){
            if(key!=key2Del) tmp[key] = obj[key];
        }
        return tmp;
    },
    /** 将一个obj加入另一个obj */
    add : function(obj, obj2Add){
        var tmp = LVT.obj.clone(obj);
        for(var key in obj2Add){
            tmp[key] = obj2Add[key];
        }
        return tmp;
    },
    /** 计算obj的hash */
    hashCode : function(obj){
        obj = JSON.stringify(obj);
        var hash = 0;
        if(obj.length == 0) return hash;
        for(var i = 0; i < obj.length; i++){
            var character = obj.charCodeAt(i);
            hash = ((hash<<5)-hash)+character;
            hash = hash & hash;
        }
        return hash;
    }
};
if(!Tobj) var Tobj=LVT.obj;
if(!$obj) var $obj=LVT.obj;

LVT.dateRegex = /^\d{4}-\d{1,2}-\d{1,2} \d{1,2}:\d{1,2}:\d{1,2}$/;
if(!TdateRegex) var TdateRegex=LVT.dateRegex;

LVT.timeRegex = /^(\d)+:(\d{1,2}):(\d{1,2})(:(\d{1,3}))?$/;
if(!TtimeRegex) var TtimeRegex=LVT.timeRegex;

/** 来自：http://urlregex.com */
LVT.urlRegex = /^((([A-Za-z]{3,9}:(?:\/\/)?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-]+|(?:www\.|[\-;:&=\+\$,\w]+@)[A-Za-z0-9\.\-]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\?\=\.\!\/\\\w]*))?)$/;
if(!TurlRegex) var TurlRegex=LVT.urlRegex;

/** 时间戳格式化为'yyyy-MM-dd HH:mm:ss'函数 */
LVT.timestampFormat = function(time){
    if(time==null) return;
    time = new Date(time);
    var year = time.getFullYear();
    var month = time.getMonth() + 1 < 10 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1;
    var date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate();
    var hour = time.getHours()< 10 ? "0" + time.getHours() : time.getHours();
    var minute = time.getMinutes()< 10 ? "0" + time.getMinutes() : time.getMinutes();
    var second = time.getSeconds()< 10 ? "0" + time.getSeconds() : time.getSeconds();
    return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
};
if(!TtimestampFormat) var TtimestampFormat=LVT.timestampFormat;
if(!jQuery.timestampFormat) jQuery.timestampFormat=LVT.timestampFormat;

/** 时间长度格式化为'HH:mm:ss'函数 */
LVT.timeFormat = function(time){
    if(time==null) return;
    time = LVT.int(time/1000);
    var s = time%60; s = s<10? "0"+s : s; time = LVT.int(time/60);
    var m = time%60; m = m<10? "0"+m : m; time = LVT.int(time/60);
    var h = time; h = h<10? "0"+h : h;
    return h+":"+m+":"+s;
};
if(!TtimeFormat) var TtimeFormat=LVT.timeFormat;
if(!jQuery.timeFormat) jQuery.timeFormat=LVT.timeFormat;

/** 时间戳格式化为'yyyy-MM-dd'函数 */
LVT.dateFormat = function(time){
    if(time==null) return;
    time = new Date(time);
    var year = time.getFullYear();
    var month = time.getMonth() + 1 < 10 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1;
    var date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate();
    return year + "-" + month + "-" + date;
};
if(!TdateFormat) var TdateFormat=LVT.dateFormat;
if(!jQuery.dateFormat) jQuery.dateFormat=LVT.dateFormat;

/** 转换序列化的字符串为json数据,出现异常返回null */
LVT.json = function(jsonStr){
    var json;
    try{
        json = eval('('+jsonStr+')');
        if(json) return json;
    }catch(e){}
    try{
        json = JSON.parse(jsonStr);
        if(json) return json;
    }catch(e){}
    try{
        json = jQuery.parseJSON(jsonStr);
        if(json) return json;
    }catch(e){}
    return null;
};
if(!Tjson) var Tjson=LVT.json;
if(!jQuery.json) jQuery.json=LVT.json;

/** JSON.stringify简化方法名 */
LVT.jsf = function(obj, replacer, space){
    return JSON.stringify(obj, replacer, space);
};
if(!Tjsf) var Tjsf=LVT.jsf;
if(!jQuery.jsf) jQuery.jsf=LVT.jsf;

/** 返回参数列表中第一个非空的参数 */
LVT.fnn = function(){
    for(var i in arguments){
        var arg = arguments[i];
        if(arg!=null) return arg;
    }
};
if(!Tfnn) var Tfnn=LVT.fnn;
if(!jQuery.fnn) jQuery.fnn=LVT.fnn;

/** 原始的parseInt会返回NaN,封装一下,遇到NaN时返回def */
LVT.int = function(str, def){
    try{
        var num = parseInt(str);
        if(isNaN(num)) return def;
        return num;
    }catch(e){}
    return def
};
if(!Tint) var Tint=LVT.int;
if(!jQuery.int) jQuery.int=LVT.int;

/** 原始的parseFloat会返回NaN,封装一下,遇到NaN时返回def */
LVT.float = function(str, def){
    try{
        var num = parseFloat(str);
        if(isNaN(num)) return def;
        return num;
    }catch(e){}
    return def
};
if(!Tfloat) var Tfloat=LVT.float;
if(!jQuery.float) jQuery.float=LVT.float;

/** 满足LVT.timeRegex的时间格式转化为数值型时间长度值 */
LVT.time = function(str, def){
    var timeGroup = str.match(LVT.timeRegex);
    if(!timeGroup) return def;
    var h = LVT.int(timeGroup[1], 0);
    var m = LVT.int(timeGroup[2], 0);
    var s = LVT.int(timeGroup[3], 0);
    var ss = LVT.int(timeGroup[5], 0);
    return h*60*60*1000 + m*60*1000 + s*1000 + ss;
};
if(!Ttime) var Ttime=LVT.time;
if(!jQuery.time) jQuery.time=LVT.time;

/**
 * 满足<br>
 * yyyy<br>
 * yyyy-MM<br>
 * yyyy-MM-dd<br>
 * yyyy-MM-dd HH<br>
 * yyyy-MM-dd HH:mm<br>
 * yyyy-MM-dd HH:mm:ss<br>
 * yyyy-MM-dd HH:mm:ss:sss<br>
 * yyyy-MM-dd HH:mm:ss:sss+08:00<br>
 * yyyy-MM-dd HH:mm:ss:sssZ<br>
 * (日期分割符'-'可以是'/',' '可以是'T',':'可以是',',时区部分忽略不处理)<br>
 * 等日期格式转化为时间戳<br>
 * 以避免不同浏览器对于new Date()的不兼容<br>
 * str参数为null或遇到异常返回def
 */
LVT.date = function(str, def){
    if(!str) return def;
    var splits = str.split(/[ TZ+]/);
    var datePart = splits[0];
    if(!datePart) return def;
    datePart = datePart.split(/[-//]/)
    var timePart = (splits[1] || '').split(/[:,]/);
    var zonePart = splits[2];
    var y = Tint(datePart[0]);
    if(y==null) return def;
    var M = Tint(datePart[1]);
    if(M==null) if(datePart[1]) return def; else M=1;
    var d = Tint(datePart[2]);
    if(d==null) if(datePart[2]) return def; else d=1;
    var H = Tint(timePart[0]);
    if(H==null) if(timePart[0]) return def; else H=0;
    var m = Tint(timePart[1]);
    if(m==null) if(timePart[1]) return def; else m=0;
    var s = Tint(timePart[2]);
    if(s==null) if(timePart[2]) return def; else s=0;
    var ss = Tint(timePart[3]);
    if(ss==null) if(timePart[3]) return def; else ss=0;
    return new Date(y,M-1,d,H,m,s,ss).getTime();
};
if(!Tdate) var Tdate=LVT.date;
if(!jQuery.date) jQuery.date=LVT.date;

/** 以B为单位的文件大小转为G/M/K/B的 */
LVT.GMKB = function(size){
    if(size<1024) return size.toFixed(2) + 'B';
    size = size/1024;
    if(size<1024) return size.toFixed(2) + 'K';
    size = size/1024;
    if(size<1024) return size.toFixed(2) + 'M';
    size = size/1024;
    if(size<1024) return size.toFixed(2) + 'G';
};
if(!TGMKB) var TGMKB=LVT.GMKB;
if(!jQuery.GMKB) jQuery.GMKB=LVT.GMKB;

/**
 * jQuery扩展函数，用于读取与设置元素的值，以布尔格式<br>
 * 如果是INPUT，且为checkbox/radio，则prop('checked')<br>
 * 如果是INPUT，且为其他type；或者SELECT；或者TEXTAREA：则读取值val()==''则是null，否则val()=='true',设置值val(''/'true'/'false')<br>
 * 其他元素，读取值text()==''则是null，否则text()=='true',设置值text(''/'true'/'false')
 */
jQuery.fn.valAsBit = function(val){
    switch(this.prop('tagName')){
    case 'INPUT':
        switch(this.prop('type')){
        case 'checkbox':
        case 'radio':
            if(val==null) return this.prop('checked');
            if('boolean' == typeof val) return this.prop('checked', val);
            val = val=='true' || val==1;
            return this.prop('checked', val);
        }
    case 'TEXTAREA':
    case 'SELECT':
        if(val==null) return this.val()==''?null:this.val()=='true';
        if('boolean' == typeof val) return this.val(val?'true':'false');
        if('string'==typeof val && ''==val) return this.val(val);
        val = val=='true' || val==1;
        return this.val(val?'true':'false');
    default:
        if(val==null) return this.val()==''?null:this.text()=='true';
        if('boolean' == typeof val) return this.text(val?'true':'false');
        if('string'==typeof val && ''==val) return this.text(val);
        val = val=='true' || val==1;
        return this.text(val?'true':'false');
    }
};
/**
 * 表单框架<br>
 */
LVT.form = {
    deserialize : function(form, data){
        var valid=new LVT.form.Valid(form);
        valid.init();
        var dataType = LVT.form.dataType.type(form, LVT.form.dataType.obj);
        if(!dataType) return;
        dataType.setter(form, data);
    },
    serialize : function(form, errMsgHolder){
        var valid=new LVT.form.Valid(form);
        valid.init();
        var dataType = LVT.form.dataType.type(form, LVT.form.dataType.obj);
        if(!dataType) return;
        var data = dataType.getter(form, valid);
        if(valid.isSuccess()) return data;
        valid.show();
        if(errMsgHolder) errMsgHolder.msg=valid.msg;
    },
    dataType : {
        type : function(ele, def){
            var attrType = LVT.form.dataAttr.type(ele);
            if(!attrType && def) return def;
            if(!attrType && !def) return LVT.form.dataType.text;
            var dataType = LVT.form.dataType[attrType];
            if(!dataType) return console.warn('不支持的data-type在', ele);
            return dataType;
        },
        text : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func](LVT.ig(val));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val.length<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val.length>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(val)) return valid.err(ele);
                return val;
            }
        },
        date : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func]('');
                if(val==null) return;
                var time = val;
                switch(typeof time){
                case 'string':
                    time = LVT.date(time);
                    break;
                case 'object':
                    time = time.time;
                    break;
                }
                if('number'!=typeof time)
                    return console.warn('尝试设置日期['+ele.attr('name')+']为['+val+']失败!');
                ele[func](LVT.timestampFormat(time));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return;
                if(!LVT.dateRegex.test(val)) return valid.err(ele);
                val=LVT.date(val);
                if(ele[func]() && val==null) return valid.err(ele);
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(ele[func]())) return valid.err(ele);
                return val;
            }
        },
        time : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func]('');
                if(val==null) return;
                var time = val;
                if('string'==typeof time) time = LVT.time(time);
                if('number'!=typeof time)
                    return console.warn('尝试设置时间['+ele.attr('name')+']为['+val+']失败!');
                ele[func](LVT.timeFormat(time));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return;
                if(!LVT.timeRegex.test(val)) return valid.err(ele);
                val = LVT.time(val);
                if(ele[func]() && val==null) return valid.err(ele);
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(ele[func]())) return valid.err(ele);
                return val;
            }
        },
        url : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func](LVT.ig(val));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return null;
                if(!LVT.urlRegex.test(val)) return valid.err(ele);
                if(ele[func]() && val==null) return valid.err(ele);
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val.length<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val.length>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(ele[func]())) return valid.err(ele);
                return val;
            }
        },
        int : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func](LVT.ig(val));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return;
                val = LVT.int(val);
                if(ele[func]() && val==null) return valid.err(ele);
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(ele[func]())) return valid.err(ele);
                return val;
            }
        },
        float : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func](LVT.ig(val));
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return;
                val = LVT.float(val);
                if(ele[func]() && val==null) return valid.err(ele);
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val>rangeNumEnd) return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(ele[func]())) return valid.err(ele);
                return val;
            }
        },
        bit : {
            setter : function(ele, val){
                var func = LVT.form.dataAttr.func(ele);
                ele[func](val==null?'':val);
            },
            getter : function(ele, valid){
                var func = LVT.form.dataAttr.func(ele);
                var val = ele[func]();
                var required = LVT.form.dataAttr.required(ele);
                if(required && val==null) return valid.err(ele);
                return val;
            }
        },
        obj : {
            setter : function(pEle, data){
                if(!data) data={};
                if('object'!=typeof data)
                    console.warn('尝试为', pEle, '设置obj', data, '异常!');
                var eles = pEle.findDirect('[name]');
                for(var i=0; i<eles.length; i++){
                    var ele = eles.eq(i);
                    var val = data[ele.attr('name')];
                    var dataType = LVT.form.dataType.type(ele);
                    if(!dataType){
                        console.warn('无法为', ele, '设置值', val);
                        continue;
                    }
                    dataType.setter(ele, val);
                }
            },
            getter : function(ele, valid){
                var data = {};
                var eles = ele.findDirect('[name]');
                for(var i=0; i<eles.length; i++){
                    var eleInner = eles.eq(i);
                    var dataType = LVT.form.dataType.type(eleInner);
                    if(!dataType){
                        console.warn('无法获取', eleInner, '的值!');
                        continue;
                    }
                    var val = dataType.getter(eleInner, valid);
                    if(valid.isFail()) return;
                    if(val==null) continue;
                    data[eleInner.attr('name')] = val;
                }
                var required = LVT.form.dataAttr.required(ele);
                if(required && LVT.obj.isEmpty(data)) return valid.err(ele);
                if(!required && LVT.obj.isEmpty(data)) return;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var size = LVT.obj.size(data);
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && size<rangeNumBegin) return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && size>rangeNumEnd) return valid.err(ele);
                }
                return data;
            }
        },
        arr : {
            setter : function(pEle, vals){
                if(!vals) vals=[];
                if(vals.length==null)
                    console.warn('尝试为', pEle, '设置arr', vals ,'异常!');
                var eles = pEle.findDirect('[name]');
                for(var i=0; i<eles.length; i++){
                    var ele = eles.eq(i);
                    var val = vals[i];
                    var dataType = LVT.form.dataType.type(ele);
                    if(!dataType){
                        console.warn('无法为', ele ,'设置值', val);
                        continue;
                    }
                    dataType.setter(ele, val);
                }
            },
            getter : function(ele, valid){
                var vals = [];
                var eles = ele.findDirect('[name]');
                for(var i=0; i<eles.length; i++){
                    var eleInner = eles.eq(i);
                    var dataType = LVT.form.dataType.type(eleInner);
                    if(!dataType){
                        console.warn('无法获取', eleInner ,'的值!');
                        continue;
                    }
                    var val = dataType.getter(eleInner, valid);
                    if(valid.isFail()) return;
                    if(val==null) continue;
                    vals.push(val);
                }
                var required = LVT.form.dataAttr.required(ele);
                if(required && vals.length==0) return valid.err(ele);
                if(!required && vals.length==0) return;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && vals.length<rangeNumBegin) 
                        return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && vals.length>rangeNumEnd)
                        return valid.err(ele);
                }
                return vals;
            }
        }
    },
    dataAttr : {
        type : function(ele){
            return ele.attr('data-type');
        },
        func : function(ele){
            var func = ele.attr('data-func');
            if(func) return func;
            switch(ele.attr('data-type')||'text'){
            case 'bit': return 'valAsBit';
            default: return 'val';
            }
        },
        required : function(ele){
            return ele.attr('data-required')!=null;
        },
        range : function(ele, valid){
            var rangeStr = ele.attr('data-range');
            if(!rangeStr) return;
            var rangeStrs = rangeStr.split('~');
            var rangeNumBegin = LVT.float(rangeStrs[0]);
            var rangeNumEnd = LVT.float(rangeStrs[1]);
            if(rangeNumBegin==null && rangeNumEnd==null) return valid.err(ele,
                    '[name='+ele.attr('name')+']的"data-range"属性错误,格式不为"[数值]~[数值]"!');
            var range = [];
            range[0] = rangeNumBegin;
            range[1] = rangeNumEnd;
            return range;
        },
        regex : function(ele, valid){
            try{
                var regexStr = ele.attr('data-regex');
                if(!regexStr) return;
                return new RegExp(regexStr);
            }catch(e){
                return valid.err(ele,
                        '[name='+ele.attr('name')+']的"data-regex"属性错误!');
            }
        },
        err : function(ele){
            var err = ele.attr('data-err');
            if(!err) return ele.attr('name')+'数据错误!';
            return err;
        }
    },
    Valid : function(form){
        this.form = form;
        this.err = function(ele, msg){
            this.ele = ele;
            this.msg = msg;
            if(!this.msg) this.msg = LVT.form.dataAttr.err(ele);
        };
        this.init = function(){
            this.form.find('.valid-err').text('');
        };
        this.show = function(){
            var validMsgEle = this.form.find('.valid-err');
            validMsgEle.text(LVT.ig(this.msg));
            if(!this.msg) return;
            var ele = this.ele;
            validMsgEle.scrollToMe(function(){
                validMsgEle.focusMe();
                ele.focusMe();
            });
        };
        this.isSuccess = function(){
            if(this.msg) return false;
            return true;
        };
        this.isFail = function(){
            return !this.isSuccess();
        }
    }
};
if(!Tform) var Tform=LVT.form;
if(!$form) var $form=LVT.form;

//------------------------------------------------------------jQuery对象方法扩展
/** 寻找目标满足选择器的最直接子元素 */
jQuery.fn.findDirect = function(selector){
    var rawEles = this.find(selector);
    var eles = jQuery();
    for(var i=0; i<rawEles.length; i++){
        var ele = rawEles.eq(i);
        if(ele.parentsUntil(this).is(selector)) continue;
        eles = eles.add(ele);
    }
    return eles;
};

/** 淡入淡出以使用户注意力转至此处 */
jQuery.fn.focusMe = function(){
    return this.fadeOut().fadeIn();
};

/** 页面滚动至指定元素 */
jQuery.fn.scrollToMe = function(callback){
    var offset = this.offset();
    if(!offset) return this;
    $('body').animate({scrollTop:offset.top}, 500, callback);
    return this;
};

/** 设置或提取元素的data数据,表现在其data属性上 */
jQuery.fn.attrData = function(data){
    if(data==null) {
        data = jQuery.json(this.attr('data'));
        if(data!=null) return data;
        try {
            data = JSON.parse(this.attr('data'));
            if(data!=null) return data;
        }catch(e){}
        try {
            data = eval('('+this.attr('data')+')');
            if(data!=null) return data;
        }catch(e){}
    }
    this.attr('data', jQuery.jsf(data));
};

/** 表单序列化及反序列化 */
jQuery.fn.formData = function(data, errMsgHolder){
    if(data==null) return LVT.form.serialize(this, errMsgHolder);
    return LVT.form.deserialize(this, data);
};