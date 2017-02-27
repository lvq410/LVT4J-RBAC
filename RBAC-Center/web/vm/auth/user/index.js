function loadUsers() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/auth/user/list.json',
        {
            proId: curPro.id,
            keyword: $('#keyword').val(),
            pager: $('#usersPager').pagerSerialize()
        },
        function(users){
            $('#users').html($tpl(tpl_users)(users));
            $('#editUserAuthDiv').slideUp();
        },
        '加载用户中'
    );
}

function editUserAuth(btn) {
    var user = $(btn).closest('tr').attrData();
    $('#userId').val(user.id);
    $('#userName').html(user.name);
    $('#userDes').html(user.des.replace(/\n/g, '<br>'));
    $('#params').html($tpl(tpl_userParams)(user.auth.params, user.auth.userParams));
    $('#role').select2Clear();
    $('#roles').html($tpl(tpl_roles)(user.auth.userRoles, true));
    $('#access').select2Clear();
    $('#accesses').html($tpl(tpl_accesses)(user.auth.userAccesses, true));
    $('#permission').select2Clear();
    $('#permissions').html($tpl(tpl_permissions)(user.auth.userPermissions, true));
    $('#allRoles').html($tpl(tpl_roles)(user.auth.allRoles, false));
    $('#allAccesses').html($tpl(tpl_accesses)(user.auth.allAccesses, false));
    $('#allPermissions').html($tpl(tpl_permissions)(user.auth.allPermissions, false));
    
    $('#editUserAuthDiv').slideDown().scrollToMe();
}

function onRAPChange() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) userAuth = {};
    userAuth.proId = curPro.id;
    q('/edit/auth/user/cal.json',
        userAuth,
        function(userAuth){
            $('#allRoles').html($tpl(tpl_roles)(userAuth.allRoles, false));
            $('#allAccesses').html($tpl(tpl_accesses)(userAuth.allAccesses, false));
            $('#allPermissions').html($tpl(tpl_permissions)(userAuth.allPermissions, false));
        },
        '计算用户所有权限中'
    );
}

function editUserAuthSave() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) userAuth = {};
    userAuth.proId = curPro.id;
    q('/edit/auth/user/set.json',
        userAuth,
        function() {
            alert('保存成功!');
            loadUsers();
        },
        '保存用户权限中'
    );
}

$(document).ready(ready);

function ready(){
    loadUsers();
}

function tpl_users(users) {
    if(!users) return;
    for (var i = 0; i < users.length; i++) {
        var user = users[i];
        var rowspan = Math.max(1, user.auth.params.length);
        var userParams = user.auth.userParams;
        var params = Tarr.clone(user.auth.params);
        if(params.length==0) params.push({});
        for (var j = 0; j < params.length; j++) {
            var param = params[j];
            param.val = userParams[param.key];
        }
        var param = params[0];
        /*<tr data="{Tigh(user)}" title="{Tigh(user.des)}">
            <td rowspan="{rowspan}">{Tigh(user.id)}</td>
            <td rowspan="{rowspan}">{Tigh(user.name)}</td>*/
        if(param.key){
            /*<td class="msg-tooltiper">
                {Tigh(param.name)}
                <div class="tooltip-msg">
                    <strong>key:</strong>{Tigh(param.key)}<br>
                    {Tigh(param.des).replace(/\n/g, '<br>')}
                </div>
            </td>
            <td class="msg-tooltiper">
                /*{Tigh(param.val).replace(/\n/g, '<br>')}
                <div class="tooltip-msg">
                    <strong>key:</strong>{Tigh(param.key)}<br>
                    {Tigh(param.des).replace(/\n/g, '<br>')}
                </div>
            </td>*/
        } else {
            /*<td></td>
            <td></td>*/
        }
            /*<td rowspan="{rowspan}">{$tpl(tpl_roles)(user.auth.allRoles)}</td>
            <td rowspan="{rowspan}">{$tpl(tpl_accesses)(user.auth.allAccesses)}</td>
            <td rowspan="{rowspan}">{$tpl(tpl_permissions)(user.auth.allPermissions)}</td>
            <td rowspan="{rowspan}"><button onclick="editUserAuth(this)" type="button" class="btn btn-info btn-minier">更改</button></td>
        </tr>*/
        for (var j = 1; j < params.length; j++) {
            var param = params[j];
            /*<tr>
                <td class="msg-tooltiper">
                    {Tigh(param.name)}
                    <div class="tooltip-msg">
                        <strong>key:</strong>{Tigh(param.key)}<br>
                        {Tigh(param.des).replace(/\n/g, '<br>')}
                    </div>
                </td>
                <td class="msg-tooltiper">
                    {Tigh(param.val).replace(/\n/g, '<br>')}
                    <div class="tooltip-msg">
                        <strong>key:</strong>{Tigh(param.key)}<br>
                        {Tigh(param.des).replace(/\n/g, '<br>')}
                    </div>
                </td>
            </tr>*/
        }
    }
}
function tpl_userParams(params, userParams) {
    if (!params) return;
    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        var val = userParams[param.key];
        /*<div class="form-group">
            <label class="col-xs-3 control-label">
                <span title="{Tigh(param.key)}">{Tigh(param.name)}：</span>
            </label>
            <div class="col-xs-9 msg-tooltiper">
                <textarea name="{Tigh(param.key)}" class="form-control" placeholder="{Tigh(param.des)}">{Tigh(val)}</textarea>
                <div class="tooltip-msg">{Tigh(param.des).replace(/\n/g, '<br>')}</div>
            </div>
        </div>*/
    }
}