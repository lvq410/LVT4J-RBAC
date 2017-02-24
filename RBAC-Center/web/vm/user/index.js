function loadUsers() {
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

function addUser() {
    $('#editUserDiv').formData({});
    $('#editUserDiv').slideDown();
}

function editUser(btn) {
    var user = $(btn).closest('tr').attrData();
    user.oldId = user.id;
    $('#editUserDiv').formData(user);
    $('#editUserDiv').slideDown();
}

function editUserSave() {
    var user = $('#editUserDiv').formData();
    if(!user) return;
    q('/edit/user/set.json',
        user,
        loadUsers,
        '保存用户中'
    );
}

function delUser(btn) {
    var user = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除用户\nID:'+user.id+'\n名称:'+user.name+'\n吗?')) return;
    q('/edit/user/del.json',
        {
            id:user.id
        },
        loadUsers,
        '删除用户中'
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
            <td>
                <button onclick="editUser(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delUser(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}