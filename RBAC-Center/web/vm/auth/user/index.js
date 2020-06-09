$(loadUsers);

function queryUsers(){
    $('#usersPager').pagerPageNo(1);
    loadUsers();
}

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
        function(data){
            $('#usersPager').pagerCount(data.count);
            $('#users').html(tpl_users(data.models));
            if($('#editUserAuthDiv').dialog('instance')) $('#editUserAuthDiv').dialog('close');
        }, '加载用户中'
    );
}

function editUserAuth(btn) {
    var user = $(btn).closest('tr').attrData();
    $('#userAutoId').val(user.autoId);
    $('#userId').val(user.id);
    $('#userName').html(user.name);
    $('#userDes').html(user.des?user.des.replace(/\n/g, '<br>'):'');
    $('#params').html(tpl_params(user.params));
    $('#roles').html(tpl_auths(user.roles, 1));
    $('#accesses').html(tpl_auths(user.accesses, 1));
    $('#permissions').html(tpl_auths(user.permissions, 1));
    $('.q-auth-search').val('');
    $('.a-auth-search').val('');
    onAuthChange();
    $('#editUserAuthDiv').dialog({
        title:'更改用户权限',
        minWidth:1200,
        buttons:{'保存':editUserAuthSave}
    });
}

function onAuthChange() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) userAuth = {};
    userAuth.proAutoId = curPro.autoId;
    q('/edit/auth/user/cal.json',
        userAuth,
        function(userAuth){
            $('#allRoles').html(tpl_allAuths(userAuth.allRoles));
            $('#allAccesses').html(tpl_allAuths(userAuth.allAccesses));
            $('#allPermissions').html(tpl_allAuths(userAuth.allPermissions));
            $('.a-auth-search').change();
        }, '计算用户所有权限中'
    );
}

function editUserAuthSave() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) return;
    userAuth.proAutoId = curPro.autoId;
    q('/edit/auth/user/set.json',
        userAuth,
        function() {
            alert('保存成功!');
            loadUsers();
        }, '保存用户权限中'
    );
}


var tpl_users = $tpl(function(users){
    if(!users) return;
    for (var i = 0; i < users.length; i++) {
        var user = users[i];
        /*<tr data="{Tigh(user)}" title="{Tigh(user.des)}">
            <td style="padding:0"><div class="list-ele">{Tigh(user.id)}</div></td>
            <td style="padding:0"><div class="list-ele">{Tigh(user.name)}</div></td>*/
            /*<td colspan="2" style="padding:0">
                <div class="list-ele"><table class="table table-striped table-bordered table-hover table-condensed" style="margin:0;"><tbody>*/
                    for (var j = 0; j < user.params.length; j++) {
                        var param = user.params[j];
                        if(!param.val) continue;
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
            /*</tbody></table></div>
            </td>*/
            /*<td style="padding:0"><div class="list-ele">{tpl_auths(user.roles, 0)}</div></td>
            <td style="padding:0"><div class="list-ele">{tpl_auths(user.accesses, 0)}</div></td>
            <td style="padding:0"><div class="list-ele">{tpl_auths(user.permissions, 0)}</div></td>
            <td><button onclick="editUserAuth(this)" type="button" class="btn btn-info btn-minier">更改</button></td>
        </tr>*/
    }
});