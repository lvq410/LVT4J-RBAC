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
                var msg = 'err:'+rst.err+'\nmsg:'+rst.msg;
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
            alert('error:'+msg+'\nobj:'+JSON.stringify(obj));
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

/** 只展示一个元素,其他隐藏 */
function slideOne(allEleIds, showEleId){
    for(var i = 0; i < allEleIds.length; i++){
        var eleId = allEleIds[i];
        $('#'+eleId).slideUp();
    }
    if(showEleId) $('#'+showEleId).slideDown().scrollToMe();
}

function onCurProIdChange(){
    if(curPro && curPro.id==$('#curProId').val()) return;
    q('/edit/curProSet',
        {
            curProId:$('#curProId').val()
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

$(document).ready(onready);

function addAccess() {
    var access = $('#access').data('select2:data');
    if(!access) return alert('请先选择访问项!');
    var accessPatterns = $('#accesses').formData();
    if(Tarr.contains(accessPatterns, access.pattern))
        return alert('已有访问项['+access.name+']!');
    $('#accesses').append($tpl(tpl_accesses)([access], true));
    if(afterAdd) afterAdd();
}
function tpl_accesses(accesses, removable) {
    if(!accesses) return;
    for (var i = 0; i < accesses.length; i++) {
        var access = accesses[i];
        /*<span class="badge badge-{getBadge(access.pattern)} msg-tooltiper" style="cursor:default;">
            {Tigh(access.name)}*/
            if(removable) {
                /*<input name value="{Tigh(access.pattern)}" type="hidden"/>
                <i onclick="$(this).closest('span').remove();if(afterAdd) afterAdd();"
                    class="ace-icon fa fa-close" style="cursor:pointer;"></i>*/
            }
            /*<div class="tooltip-msg">
                <strong>pattern:</strong>{Tigh(access.pattern)}<br>
                <strong>名称:</strong>{Tigh(access.name)}<br>
                {Tigh(access.des).replace(/\n/g, '<br>')}<br>
            </div>
        </span>*/
    }
}

function addPermission() {
    var permission = $('#permission').data('select2:data');
    if(!permission) return alert('请先选择授权项!');
    var permissionIds = $('#permissions').formData();
    if(Tarr.contains(permissionIds, permission.id))
        return alert('已有授权项['+permission.name+']!');
    $('#permissions').append($tpl(tpl_permissions)([permission], true));
    if(afterAdd) afterAdd();
}
function tpl_permissions(permissions, removable) {
    if(!permissions) return;
    for (var i = 0; i < permissions.length; i++) {
        var permission = permissions[i];
        /*<span class="badge badge-{getBadge(permission.id)} msg-tooltiper" style="cursor:default;">
            {Tigh(permission.name)}*/
            if(removable) {
                /*<input name value="{Tigh(permission.id)}" type="hidden"/>
                <i onclick="$(this).closest('span').remove();if(afterAdd) afterAdd();"
                    class="ace-icon fa fa-close" style="cursor:pointer;"></i>*/
            }
            /*<div class="tooltip-msg">
                <strong>ID:</strong>{Tigh(permission.id)}<br>
                <strong>名称:</strong>{Tigh(permission.name)}<br>
                {Tigh(permission.des).replace(/\n/g, '<br>')}<br>
            </div>
        </span>*/
    }
}

function addRole() {
    var role = $('#role').data('select2:data');
    if(!role) return alert('请先选择角色!');
    var roles = $('#roles').formData();
    if(Tarr.contains(roles, role.id))
        return alert('已有角色['+role.name+']!');
    $('#roles').append($tpl(tpl_roles)([role], true));
    if(afterAdd) afterAdd();
}
function tpl_roles(roles, removable) {
    if(!roles) return;
    for (var i = 0; i < roles.length; i++) {
        var role = roles[i];
        /*<span class="badge badge-{getBadge(role.id)} msg-tooltiper" style="cursor:default;">
            {Tigh(role.name)}*/
            if(removable) {
                /*<input name value="{Tigh(role.id)}" type="hidden"/>
                <i onclick="$(this).closest('span').remove();if(afterAdd) afterAdd();"
                    class="ace-icon fa fa-close" style="cursor:pointer;"></i>*/;
            }
            /*<div class="tooltip-msg">
                <strong>ID:</strong>{Tigh(role.id)}<br>
                <strong>名称:</strong>{Tigh(role.name)}<br>
                {Tigh(role.des).replace(/\n/g, '<br>')}<br>
            </div>
        </span>*/
    }
}

var BadgeClses = ['success','warning','danger','info','purple','pink','yellow'];
function getBadge(key) {
    return BadgeClses[Math.abs(Tobj.hashCode(key))%BadgeClses.length];
}