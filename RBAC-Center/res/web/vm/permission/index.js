function loadPermissions(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/permission/list.json',
        {
            proId: curPro.id,
            keyword: $('#keyword').val(),
            pager: $('#permissionsPager').pagerSerialize()
        },
        function(permissions){
            $('#permissions').html($tpl(tpl_permissions)(permissions));
            $('#editPermissionDiv').slideUp();
            $('#permissionsDiv').slideDown();
        },
        '加载授权项中'
    );
}

function addPermission() {
    $('#editPermissionDiv').formData({});
    $('#editPermissionDiv').slideDown().scrollToMe();
}

function editPermission(btn) {
    var permission = $(btn).closest('tr').attrData();
    permission.oldId = permission.id;
    $('#editPermissionDiv').formData(permission);
    $('#editPermissionDiv').slideDown().scrollToMe();
}

function editPermissionSave() {
    var permission = $('#editPermissionDiv').formData();
    if(!permission) return;
    permission.proId = curPro.id;
    q('/edit/permission/set.json',
        permission,
        function() {
            alert('保存成功!');
            loadPermissions();
        },
        '保存授权项中'
    );
}

function delPermission(btn) {
    var permission = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除授权项\nID:'+permission.id+'\n名称:'+permission.name+'\n吗?')) return;
    q('/edit/permission/del.json',
        {
            proId:curPro.id,
            id:permission.id
        },
        function() {
            alert('删除成功!');
            loadPermissions();
        },
        '删除授权项中'
    );
}

$(document).ready(ready);

function ready(){
    loadPermissions();
}

function tpl_permissions(permissions) {
    if(!permissions) return;
    for (var i = 0; i < permissions.length; i++) {
        var permission = permissions[i];
        /*<tr data="{Tigh(permission)}" title="{Tigh(permission.des)}">
            <td>{Tigh(permission.id)}</td>
            <td>{Tigh(permission.name)}</td>
            <td>
                <button onclick="editPermission(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delPermission(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}