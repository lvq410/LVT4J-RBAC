function loadUsers() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/user/list.json',
        {
            keyword: $('#keyword').val(),
            pager: $('#usersPager').pagerSerialize()
        },
        function(users){
            $('#users').html($tpl(tpl_users)(users));
            $('#editUserDiv').slideUp();
        },
        '加载用户中'
    );
}

function editUserAuth(btn) {
    var user = $(btn).closest('tr').attrData();
    q('/edit/user/getAuth.json',
        {
            proId: curPro.id,
            id : user.id
        },
        function(userAuth){
            $('#userId').val(userAuth.user.id);
            $('#userName').html(userAuth.user.name);
            $('#userDes').html(userAuth.user.des.replace(/\n/g, '<br>'));
            $('#accesses').html($tpl(tpl_accesses)(userAuth.userAccesses, true));
            $('#permissions').html($tpl(tpl_permissions)(userAuth.userPermissions, true));
            $('#mergeAccesses').html($tpl(tpl_accesses)(userAuth.mergeAccesses, false));
            $('#mergePermissions').html($tpl(tpl_permissions)(userAuth.userPermissions, false));
            $('#editUserAuthDiv').slideDown();
        },
        '加载用户权限中'
    );
}

function afterAdd() {
    var userAuth = $('#editUserAuthDiv').formData();
    userAuth.proId = curPro.id;
    q('/edit/user/calAuth.json',
        userAuth,
        function(userAuth){
            $('#mergeAccesses').html($tpl(tpl_accesses)(userAuth.mergeAccesses, false));
            $('#mergePermissions').html($tpl(tpl_permissions)(userAuth.mergePermissions, false));
        },
        '计算用户最终权限中'
    );
}

function editUserAuthSave() {
    var userAuth = $('#editUserAuthDiv').formData();
    if(!userAuth) return;
    userAuth.proId = curPro.id;
    userAuth.params = {};
    q('/edit/user/setAuth.json',
        userAuth,
        loadUsers,
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
        /*<tr data="{Tigh(user)}" title="{Tigh(user.des)}">
            <td>{Tigh(user.id)}</td>
            <td>{Tigh(user.name)}</td>
            <td><button onclick="editUserAuth(this)" type="button" class="btn btn-info btn-minier">更改</button></td>
        </tr>*/
    }
}