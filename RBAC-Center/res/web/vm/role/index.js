function loadRoles(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/role/list.json',
        {
            proId: curPro.id,
            keyword: $('#keyword').val(),
            accessPattern: $('#qAccess').val(),
            permissionId: $('#qPermission').val(),
            pager: $('#rolesPager').pagerSerialize()
        },
        function(roles){
            $('#roles').html($tpl(tpl_roles)(roles));
            $('#editRoleDiv').slideUp();
            $('#rolesDiv').slideDown();
        },
        '加载角色中'
    );
}

function addRole() {
    $('#editRoleDiv').formData({});
    $('#access').select2Clear();
    $('#accesses').empty();
    $('#permission').select2Clear();
    $('#permissions').empty();
    $('#editRoleDiv').slideDown(function () {
        $('#editRoleDiv').scrollToMe();
    });
}

function editRole(btn) {
    var role = $(btn).closest('tr').attrData();
    role.oldId = role.id;
    $('#editRoleDiv').formData(role);
    $('#access').select2Clear();
    $('#accesses').html($tpl(tpl_accesses)(role.accesses, true));
    $('#permission').select2Clear();
    $('#permissions').html($tpl(tpl_permissions)(role.permissions, true));
    $('#editRoleDiv').slideDown(function () {
        $('#editRoleDiv').scrollToMe();
    });
}

function editRoleSave() {
    var role = $('#editRoleDiv').formData();
    if(!role) return;
    role.proId = curPro.id;
    q('/edit/role/set.json',
        role,
        function(){
            alert('保存成功!');
            loadRoles();
        },
        '保存角色中'
    );
}

function delRole(btn) {
    var role = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除角色\nID:'+role.id+'\n名称:'+role.name+'\n吗?')) return;
    q('/edit/role/del.json',
        {
            proId:curPro.id,
            id:role.id
        },
        function() {
            alert('删除成功!');
            loadRoles();
        },
        '删除角色中'
    );
}

$(document).ready(ready);

function ready(){
    loadRoles();
}

function tpl_roles(roles) {
    if(!roles) return;
    for (var i = 0; i < roles.length; i++) {
        var role = roles[i];
        /*<tr data="{Tigh(role)}" title="{Tigh(role.des)}">
            <td>{Tigh(role.id)}</td>
            <td>{Tigh(role.name)}</td>
            <td>{$tpl(tpl_accesses)(role.accesses)}</td>
            <td>{$tpl(tpl_permissions)(role.permissions)}</td>
            <td>
                <button onclick="editRole(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delRole(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}