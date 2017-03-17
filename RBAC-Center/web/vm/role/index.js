function loadRoles(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/role/list.json',
        {
            proAutoId: curPro.autoId,
            keyword: $('#keyword').val(),
            accessAutoId: $('#qAccess').val(),
            permissionAutoId: $('#qPermission').val(),
            needAuth:true,
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
    $('#accesses').empty();
    $('#permissions').empty();
    $('.q-auth-search').val('');
    $('#editRoleDiv').slideDown(function () {
        $('#editRoleDiv').scrollToMe();
    });
}

function sortRole() {
    var autoIds = [];
    $('#roles tr').each(function(){
        autoIds.push($(this).attrData().autoId);
    });
    if(autoIds.length==0) return alert('无排序内容!');
    q('/edit/role/sort.json',
        {
            autoIds: autoIds,
        },
        function(){
            alert('保存排序成功!');
        },
        '保存排序中'
    );
}

function editRole(btn) {
    var role = $(btn).closest('tr').attrData();
    $('#editRoleDiv').formData(role);
    $('#accesses').html($tpl(tpl_auths)(role.accesses, 1));
    $('#permissions').html($tpl(tpl_auths)(role.permissions, 1));
    $('.q-auth-search').val('');
    $('#editRoleDiv').slideDown(function () {
        $('#editRoleDiv').scrollToMe();
    });
}

function editRoleSave() {
    var role = $('#editRoleDiv').formData();
    if(!role) return;
    role.proAutoId = curPro.autoId;
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
            proAutoId:curPro.autoId,
            autoId:role.autoId
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
            <td class="sortabler-handler"><i class="ace-icon fa fa-arrows-v"></i></td>
            <td><div class="list-ele">{Tigh(role.id)}</td>
            <td><div class="list-ele">{Tigh(role.name)}</td>
            <td><div class="list-ele">{$tpl(tpl_auths)(role.accesses, 0)}</td>
            <td><div class="list-ele">{$tpl(tpl_auths)(role.permissions, 0)}</td>
            <td>
                <button onclick="editRole(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delRole(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}