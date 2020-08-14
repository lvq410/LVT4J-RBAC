/** 重写alert函数,让alert更好看点 */
function alert(titleOrMsg,msg) {
    if (msg==null) {
        $('#alertTitle').text("注意!");
        $('#alertMsg').text(titleOrMsg);
    } else {
        $('#alertLbl').text(titleOrMsg);
        $('#alertMsg').text(msg);
    }
    $('#alerter').modal('show');
}

function delayCall(callback){
    var time = window.delayCallTime = $.now();
    setTimeout(function(){
        if(time!=window.delayCallTime) return;
        callback();
    }, 500);
}

/**
 * 封装基本的ajax调用<br>
 * 返回值出现异常的处理<br>
 * 未出现异常会将返回的data传给callback处理
 */
function q(url, data, callback, waitingMsg, jsonp) {
    if(waitingMsg!=null) Tloader.show(waitingMsg);
    var sendData;
    if(data) { //忽略掉待发送数据中为null的key
        sendData = {};
        for(var key in data){
            var val = data[key];
            if (val==null) continue;
            if ('number' == typeof val) sendData[key] = val;
            else if ('string' == typeof val) sendData[key] = val;
            else sendData[key] = JSON.stringify(val);
        }
    }
    var ajax = {
        url:url,
        type:'post',
        dataType:'json',
        data:sendData,
        traditional:true,
        success:function(rst){
            if(waitingMsg!=null) Tloader.hide(waitingMsg);
            console.info(waitingMsg, rst);
            if (rst.err==0) {
                callback(rst.data, rst.msg);
            } else {
                var msg = 'err:'+rst.err+'\nmsg:';
                if(501==rst.err) msg += 'ID或pattern冲突,请另选新值填写!';
                if(404==rst.err) msg += '部分数据已移除,请刷新页面重试!';
                if(rst.msg) msg+=rst.msg;
                if(rst.stack) {
                    for (var i = 0; i < rst.stack.length; i++) {
                        msg += '\n'+rst.stack[i]
                    }
                }
                alert(msg);
            }
        },
        error:function(request, msg, obj){
            if(waitingMsg!=null) Tloader.hide(waitingMsg);
            var alertMsg = [];
            if(request && request.status){
                if(request.status==0) alertMsg.push('与服务器连接断开');
                else if(request.status) alertMsg.push('状态码:'+request.status);
            }
            alertMsg.push('url:'+url);
            alertMsg.push('请求数据:'+Tjsf(data));
            if(msg) alertMsg.push('错误消息:'+msg);
            if(obj) alertMsg.push('obj:'+JSON.stringify(obj));
            if(request.responseJSON) {
                var rst = request.responseJSON;
                if(request.status!=400&&request.status!=409&&rst.err!=null) alertMsg.splice(0,0,'error:'+rst.err);
                if(rst.msg!=null) alertMsg.splice(1,0,'msg:'+rst.msg);
                if(request.status!=400&&request.status!=409&&rst.stack!=null) alertMsg.push('stack:'+rst.stack.join('\n'));
            }
            alert('请求失败!', alertMsg.join('\n'));
        }
    };
    if(jsonp){
        ajax.type='get';
        ajax.jsonp=jsonp;
        ajax.dataType='jsonp';
        ajax.success = function(rst){
            if(waitingMsg!=null) Tloader.hide(waitingMsg);
            console.info(waitingMsg, rst);
            callback(rst);
        }
    }
    $.ajax(ajax);
}

/** 表单一个key代表/,|，|\r|\n|\t| |　|;|；/分隔的一批id，将其整理成数组形式，移除空字符串id,若为空数组，则会从表单数据中delete该idsKey，返回整理后的数据（可为null） */
function splitIds(formData, idsKey){
    var val = formData[idsKey];
    if(val==null) {
        delete formData[idsKey];
        return;
    }
    var ids = Tarr.remove(val.split(/,|，|\r|\n|\t| |　|;|；/), '');
    if(ids.length) return formData[idsKey] = ids;
    delete formData[idsKey];
    return;
}

function onCurProIdChange(){
    if(curPro && curPro.autoId==$('#curProId').val()) return;
    q('/edit/curProSet.json',
        {
            proAutoId:$('#curProId').val()
        },
        function() {
            alert('当前产品切换成功,正在刷新页面!');
            setTimeout(function(){location.reload();}, 1500);
        },
        '重置当前选中的产品中'
    );
}

/** 设置当前产品信息 */
function setCurPro() {
    if(!curPro) return;
    $('#curProId').select2Set(curPro);
}

/** 页面基础初始化 */
function onready(){
    setCurPro();
}

$(onready);

/** type 0:展示用;1:编辑用;2选择用: */
var tpl_auths = $tpl(function(auths, type){
    if(!auths) return '';
    var cursor = {0:'default',1:'move',2:'pointer'}[type];
    for (var i = 0; i < auths.length; i++) {
        var auth = auths[i];
        if(!auth) continue;
        /*<span data="{Tigh(auth)}" class="badge badge-{getBadge(auth.autoId)} msg-tooltiper" style="cursor:{cursor};"*/
            if(type==2){/* onclick="widget_auth_choose(this)"*/}/*>*/
            /*<span class="auth-name">{Tigh(auth.name)}</span>*/
            if(type==1) {
                /*<input name data-type="int" value="{Tigh(auth.autoId)}" type="hidden"/>
                <i onclick="removeAuth(this)"
                    class="ace-icon fa fa-close" style="cursor:pointer;"></i>*/
            }
            /*<div class="tooltip-msg">
                <strong>{Tigh(Tfnn(auth.id, auth.pattern))}</strong><br>
                {Tigh(auth.name)}<br>
                {Tigh(auth.des).replace(/\n/g, '<br>')}
            </div>
        </span>*/
    }
});
function removeAuth(btn) {
    $(btn).closest('span').remove();
    if(window.onAuthChange) onAuthChange();
}

var BadgeClses = ['success','warning','danger','info','purple','pink','yellow'];
function getBadge(key) {
    return BadgeClses[Math.abs(Tobj.hashCode(key))%BadgeClses.length];
}

var tpl_params = $tpl(function(params){
    if (!params) return;
    for (var i=0; i<params.length; i++) {
        var param = params[i];
        /*<div class="form-group">
            <label class="col-xs-3 control-label">
                <span title="{Tigh(param.key)}">{Tigh(param.name)}：</span>
            </label>
            <div class="col-xs-9 msg-tooltiper">
                <textarea name="{Tigh(param.autoId)}" autocomplete="off" class="form-control" placeholder="{Tigh(param.des)}">{Tigh(param.val)}</textarea>
                <div class="tooltip-msg">{Tigh(param.des).replace(/\n/g, '<br>')}</div>
            </div>
        </div>*/
    }
});

var tpl_allAuths = $tpl(function(authDescs){
    if(!authDescs) return '';
    for(var i=0; i<authDescs.length; i++){
        var authDesc = authDescs[i];
        if(!authDesc.des) authDesc.des='单独分配';
        if(authDesc.auths.length==0) continue;
        /*<tr>
            <td>{Tigh(authDesc.des)}</td>
            <td>{tpl_auths(authDesc.auths, 0)}</td>
        </tr>*/
    }
});

function all_auth_search(input){
    var tbl = $(input).closest('table');
    var keyword = $(input).val();
    tbl.find('.badge').each(function(){
        var authBadge = $(this);
        if(!keyword) authBadge.show();
        var auth = authBadge.attrData();
        if(auth.id && auth.id.indexOf(keyword)!=-1) return authBadge.show();
        if(auth.pattern && auth.pattern.indexOf(keyword)!=-1) return authBadge.show();
        if(auth.name && auth.name.indexOf(keyword)!=-1) return authBadge.show();
        authBadge.hide();
    });
}