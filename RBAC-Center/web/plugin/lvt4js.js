/**
 * 各种js小工具<br>
 * 加入了各种jQuery扩展
 * Author ： LV
 * require : jquery 1.6+
 */
var LVT = {};
if(!T) var T = LVT;
/**
 * checkbox联动效果实现
 * 主checkbox添加forgroup参数 子checkbox添加group参数
 * 需要在页面载入完成后调用LVT.cbxRely.init()
 * 页面动态生成从checkbox后也需要调用LVT.cbxRely.init()
 */
LVT.cbxRely = {
    changeSub:function(){
        var groupName = $(this).attr('group');
        $(":checkbox[forgroup="+groupName+"]").prop('checked',
                $(":checkbox[group="+groupName+"]").length==$(":checked[group="+groupName+"]").length);
    },
    changeGroup:function(){
        $(":checkbox[group="+$(this).attr('forgroup')+"]").prop('checked',$(this).prop('checked'));
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
     * 从数组删除一个元素，若已有该元素则忽略
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
 * 解析请求中的参数
 */
LVT.params = function(){
    var params = {};
    if(location.search){
        var search = location.search.substring(1,location.search.length).split("&");
        for(var i = 0; i < search.length; i++){
            var eIdx = search[i].indexOf('=');
            params[search[i].substring(0,eIdx)] =  decodeURI(search[i].substring(eIdx+1,search[i].length));
        }
    }
    return params;
};
if(!Tparams) var Tparams=LVT.params;
if(!jQuery.params) jQuery.params=LVT.params;

/**
 * 弹出一个浮在最上方,遮住全部的loading图案<br>
 * 采用延迟机制，若调用LVT.loader.show()的LVT.loader.threshold(默认300)ms后，没有调用LVT.loader.hide(),才出现
 */
LVT.loader = {
    container : null,
    msger : null,
    msgs : [],
    threshold : 300,
    init : function(){
        if(LVT.loader.container) return;
        LVT.loader.container = jQuery('<div style="position:fixed;width:100%;height:100%;background-color:rgba(0,0,0,0.23);z-index:20000;display:none;">'+
                '<div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;"><div>'+
                '<div style="text-align:center;"><img src="https://raw.githubusercontent.com/lvq410/LVT4JS/master/img/loader.gif"></div>'+
                '<div class="lvt-loader-msger" style="text-align:center;color:white;font-weight:bolder;font-size:xx-large;"></div>'+
                '</div></div>'+
        '</div>');
        LVT.loader.msger = LVT.loader.container.find('.lvt-loader-msger');
        $('body').prepend(LVT.loader.container);
    },
    show : function(msg){
        LVT.loader.init();
        var loadingMsgObj = LVT.loader.loadingMsgObj(msg);
        LVT.loader.msgs.push(loadingMsgObj);
        LVT.loader.msger.append(loadingMsgObj.ele);
        setTimeout('LVT.loader.internalShow()', LVT.loader.threshold);
    },
    immediateShow : function(msg){
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
    }
};
if(!Tloader) var Tloader=LVT.loader;
if(!$loader) var $loader=LVT.loader;
/**
 * 回避null,undefined,NaN,输出空字符串
 */
LVT.ig = function(val){
    if(val==null || val==undefined) return '';
    if(typeof val =='number' && isNaN(val)) return '';
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
    /** 判断一个object是否为空 */
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

/** 匹配url的正则 */
LVT.urlRegex = new RegExp('^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$');
if(!TurlRegex) var TurlRegex=LVT.urlRegex;

LVT.dateRegex = /^\d{4}-\d{1,2}-\d{1,2} \d{1,2}:\d{1,2}:\d{1,2}$/;
if(!TdateRegex) var TdateRegex=LVT.dateRegex;

LVT.timeRegex = /^(\d)+:(\d{1,2}):(\d{1,2})(:(\d{1,3}))?$/;
if(!TtimeRegex) var TtimeRegex=LVT.timeRegex;

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

/** 转换序列化的字符串为json数据,出现异常返回null */
LVT.json = function(jsonStr){
    try{
        return eval('('+jsonStr+')');
    }catch(e){}
    return null;
};
if(!Tjson) var Tjson=LVT.json;
if(!jQuery.json) jQuery.json=LVT.json;

/** JSON.stringify简化方法名 */
LVT.jsf = function(obj){
    return JSON.stringify(obj);
};
if(!Tjsf) var Tjsf=LVT.jsf;
if(!jQuery.jsf) jQuery.jsf=LVT.jsf;

/** 返回参数列表中第一个非空的参数 */
LVT.fnn = function(){
    for( var i in arguments){
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

/** 满足LVT.timeRegex的时间格式转化为数值型时间长度 */
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
 * 表单框架<br>
 */
LVT.form = {
    deserialize : function(form, data){
        form.find('.valid-err').text('');
        var dataType = LVT.form.dataType.type(form, LVT.form.dataType.obj);
        if(!dataType) return;
        dataType.setter(form, data);
    },
    serialize : function(form){
        var dataType = LVT.form.dataType.type(form, LVT.form.dataType.obj);
        if(!dataType) return;
        var valid=new LVT.form.Valid(form);
        var data = dataType.getter(form, valid);
        valid.show();
        return data;
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
                ele.val(LVT.ig(val));
            },
            getter : function(ele, valid){
                var val = ele.val();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return null;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val.length<rangeNumBegin) 
                        return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val.length>rangeNumEnd)
                        return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(val)) return valid.err(ele);
                return val;
            }
        },
        date : {
            setter : function(ele, val){
                ele.val('');
                if(val==null) return;
                var time = val;
                switch (typeof time){
                case 'string':
                    try{
                        time = new Date(time).getTime();
                    }catch(e){
                        return console.warn('尝试设置日期['+ele.attr('name')+']为['+val+']失败!');
                    }
                    break;
                case 'object':
                    time = time.time;
                    break;
                }
                if('number'!=typeof time)
                    return console.warn('尝试设置日期['+ele.attr('name')+']为['+val+']失败!');
                ele.val(LVT.timestampFormat(time));
            },
            getter : function(ele, valid){
                var val = ele.val();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return null;
                if(!LVT.dateRegex.test(val)) return valid.err(ele);
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(val)) return valid.err(ele);
                try{
                    return new Date(val).getTime();
                }catch(e){}
                return valid.err(ele);
            }
        },
        time : {
            setter : function(ele, val){
                ele.val('');
                if(val==null) return;
                var time = val;
                if('string'==typeof time) time = LVT.time(time);
                if('number'!=typeof time)
                    return console.warn('尝试设置时间['+ele.attr('name')+']为['+val+']失败!');
                ele.val(LVT.timeFormat(time));
            },
            getter : function(ele, valid){
                var val = ele.val();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return null;
                if(!LVT.timeRegex.test(val)) return valid.err(ele);
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && !regex.test(val)) return valid.err(ele);
                return LVT.time(val);
            }
        },
        url : {
            setter : function(ele, val){
                ele.val(LVT.ig(val));
            },
            getter : function(ele, valid){
                var val = ele.val();
                var required = LVT.form.dataAttr.required(ele);
                if(required && !val) return valid.err(ele);
                if(!required && !val) return null;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val.length<rangeNumBegin) 
                        return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val.length>rangeNumEnd)
                        return valid.err(ele);
                }
                if(!LVT.urlRegex.test(val)) return valid.err(ele);
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && val!=null && !regex.test(ele.val())) return valid.err(ele);
                return val;
            }
        },
        int : {
            setter : function(ele, val){
                ele.val(LVT.ig(val));
            },
            getter : function(ele, valid){
                var val = LVT.int(ele.val());
                var required = LVT.form.dataAttr.required(ele);
                if(required && val==null) return valid.err(ele);
                if(!required && val==null) return null;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && val<rangeNumBegin) 
                        return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && val>rangeNumEnd)
                        return valid.err(ele);
                }
                var regex = LVT.form.dataAttr.regex(ele, valid);
                if(valid.isFail()) return;
                if(regex && val!=null){
                    if(!regex.test(ele.val())) return valid.err(ele);
                }
                return val;
            }
        },
        bit : {
            setter : function(ele, val){
                ele.prop('checked', val?true:false);
            },
            getter : function(ele, valid){
                return ele.prop('checked');
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
                    var ele = eles.eq(i);
                    var dataType = LVT.form.dataType.type(ele);
                    if(!dataType){
                        console.warn('无法获取', ele, '的值!');
                        continue;
                    }
                    var val = dataType.getter(ele, valid);
                    if(valid.isFail()) return;
                    if(val==null) continue;
                    data[ele.attr('name')] = val;
                }
                var required = LVT.form.dataAttr.required(ele);
                if(required && LVT.obj.isEmpty(data)) return valid.err(ele);
                if(!required && LVT.obj.isEmpty(data)) return null;
                var range = LVT.form.dataAttr.range(ele, valid);
                if(valid.isFail()) return;
                if(range){
                    var size = LVT.obj.size(data);
                    var rangeNumBegin = range[0];
                    if(rangeNumBegin!=null && size<rangeNumBegin) 
                        return valid.err(ele);
                    var rangeNumEnd = range[1];
                    if(rangeNumEnd!=null && size>rangeNumEnd)
                        return valid.err(ele);
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
                    var ele = eles.eq(i);
                    var dataType = LVT.form.dataType.type(ele);
                    if(!dataType){
                        console.warn('无法获取', ele ,'的值!');
                        continue;
                    }
                    vals.push(dataType.getter(ele, valid));
                    if(valid.isFail()) return;
                }
                var required = LVT.form.dataAttr.required(ele);
                if(required && vals.length==0) return valid.err(ele);
                if(!required && vals.length==0) return null;
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
        required : function(ele){
            return ele.attr('data-required')!=null;
        },
        range : function(ele, valid){
            var rangeStr = ele.attr('data-range');
            if(!rangeStr) return;
            var rangeStrs = rangeStr.split('~');
            var rangeNumBegin = LVT.float(rangeStrs[0]);
            var rangeNumEnd = LVT.int(rangeStrs[1]);
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
    if(data==null) return jQuery.json(this.attr('data'));
    this.attr('data', jQuery.jsf(data));
};

/** 表单序列化及反序列化 */
jQuery.fn.formData = function(data){
    if(data) return LVT.form.deserialize(this, data);
    return LVT.form.serialize(this);
};