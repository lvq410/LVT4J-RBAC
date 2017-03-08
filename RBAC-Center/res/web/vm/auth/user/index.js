function loadUsers() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/user/list.json',
        {
            proAutoId: curPro.autoId,
            roleAutoId: $('#qRole').val(),
            accessAutoId: $('#qAccess').val(),
            permissionAutoId: $('#qPermission').val(),
            needAuth: true,
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
    $('#userAutoId').val(user.autoId);
    $('#userId').val(user.id);
    $('#userName').html(user.name);
    $('#userDes').html(user.des?user.des.replace(/\n/g, '<br>'):'');
    $('#params').html($tpl(tpl_params)(user.params));
    $('#roles').html($tpl(tpl_auths)(user.roles, 1));
    $('#accesses').html($tpl(tpl_auths)(user.accesses, 1));
    $('#permissions').html($tpl(tpl_auths)(user.permissions, 1));
    
    $('#editUserAuthDiv').slideDown(function () {
        $('#editUserAuthDiv').scrollToMe();
    });
    onAuthChange();
}

function onAuthChange() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) userAuth = {};
    userAuth.proAutoId = curPro.autoId;
    q('/edit/auth/user/cal.json',
        userAuth,
        function(userAuth){
            $('#allRoles').html($tpl(tpl_allAuths)(userAuth.allRoles));
            $('#allAccesses').html($tpl(tpl_allAuths)(userAuth.allAccesses));
            $('#allPermissions').html($tpl(tpl_allAuths)(userAuth.allPermissions));
//            $('#allRoles').html($tpl(tpl_roles)(userAuth.allRoles, false));
//            $('#allAccesses').html($tpl(tpl_accesses)(userAuth.allAccesses, false));
//            $('#allPermissions').html($tpl(tpl_permissions)(userAuth.allPermissions, false));
        },
        '计算用户所有权限中'
    );
}

function editUserAuthSave() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) userAuth = {};
    userAuth.proAutoId = curPro.autoId;
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
        var params = Tarr.clone(user.params);
        if(params.length==0) params.push({});
        /*<tr data="{Tigh(user)}" title="{Tigh(user.des)}">
            <td><div class="list-ele">{Tigh(user.id)}</div></td>
            <td><div class="list-ele">{Tigh(user.name)}</div></td>*/
            /*<td colspan="2">
                <div class="list-ele"><table class="table table-striped table-bordered table-hover table-condensed" style="margin:0;">
                    <tbody>
                        */
        for (var j = 0; j < params.length; j++) {
            var param = params[j];
            /*<tr>
                <td class="msg-tooltiper" style="width:94px;">
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
        /*
                    </tbody>
                </table></div>
            </td>*/
            /*<td><div class="list-ele">{$tpl(tpl_auths)(user.roles, 0)}</div></td>
            <td><div class="list-ele">{$tpl(tpl_auths)(user.accesses, 0)}</div></td>
            <td><div class="list-ele">{$tpl(tpl_auths)(user.permissions, 0)}</div></td>
            <td><button onclick="editUserAuth(this)" type="button" class="btn btn-info btn-minier">更改</button></td>
        </tr>*/
    }
}