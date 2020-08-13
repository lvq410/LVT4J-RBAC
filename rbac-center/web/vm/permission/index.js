$(loadPermissions);

function queryPermissions(){
    $('#permissionsPager').pagerPageNo(1);
    loadPermissions();
}

function loadPermissions(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/permission/list.json',
        {
            proAutoId: curPro.autoId,
            keyword: $('#keyword').val(),
            pager: $('#permissionsPager').pagerSerialize()
        },
        function(data){
            $('#permissionsPager').pagerCount(data.count);
            $('#permissions').html(tpl_permissions(data.models));
            if($('#editPermissionDiv').dialog('instance')) $('#editPermissionDiv').dialog('close');
        }, '加载授权项中'
    );
}

function addPermission() {
    $('#editPermissionDiv').formData({});
    $('#editPermissionDiv').dialog({
        title:'新建授权项',
        minWidth:1000,
        buttons:{'保存':editPermissionSave}
    });
}

function sortPermission() {
    var autoIds = [];
    $('#permissions tr').each(function(){
        autoIds.push($(this).attrData().autoId);
    });
    if(autoIds.length==0) return alert('无排序内容!');
    if(!confirm('确定将当前内容按所展示的顺序排序吗')) return;
    q('/edit/permission/sort.json',
        {
            autoIds: autoIds,
        },
        function(){
            alert('保存排序成功!');
        }, '保存排序中'
    );
}

function editPermission(btn) {
    var permission = $(btn).closest('tr').attrData();
    q('/edit/permission/get.json', {autoId:permission.autoId}, function(data){
        if(!data){
            loadPermissions();
            return alert('授权项不存在');
        }
        $('#editPermissionDiv').formData(data);
        $('#editPermissionDiv').dialog({
            title:'修改授权项',
            minWidth:1000,
            buttons:{'保存':editPermissionSave}
        });
    }, '加载授权项中');
}

function testMatch() {
    var text = $('#testMatchText').val();
    if(!text) return $('#testMatchRst').removeClass('text-danger').addClass('text-info').text('');
    q('/edit/testMatch.json',
        {
            regex: $('#editPermissionDiv [name=id]').val(),
            text: text
        },
        function(match){
            if(match) {
                $('#testMatchRst').removeClass('text-danger').addClass('text-info').text('匹配');
            } else {
                $('#testMatchRst').removeClass('text-info').addClass('text-danger').text('不匹配');
            }
        }, '验证授权匹配中'
    );
}

function editPermissionSave() {
    var permission = $('#editPermissionDiv').formData();
    if(!permission) return;
    try{
        new RegExp(permission.id)
    }catch(e){
        $('#editPermissionDiv .valid-err').text('配置项id不是一个正则表达式!');
        $('#editPermissionDiv [name=id]').focusMe();
        return;
    }
    permission.proAutoId = curPro.autoId;
    q('/edit/permission/set.json',
        permission,
        function() {
            alert('保存成功!');
            loadPermissions();
        }, '保存授权项中'
    );
}

function delPermission(btn) {
    var permission = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除授权项\nID:'+permission.id+'\n名称:'+permission.name+'\n吗?')) return;
    q('/edit/permission/del.json',
        {
            proAutoId: curPro.autoId,
            autoId: permission.autoId
        },
        function() {
            alert('删除成功!');
            loadPermissions();
        }, '删除授权项中'
    );
}


var tpl_permissions = $tpl(function(permissions){
    if(!permissions) return;
    for (var i = 0; i < permissions.length; i++) {
        var permission = permissions[i];
        /*<tr data="{Tigh(permission)}" title="{Tigh(permission.des)}">
            <td class="sortabler-handler"><i class="ace-icon fa fa-arrows-v"></i></td>
            <td style="padding:0"><div class="list-ele">{Tigh(permission.id)}</div></td>
            <td style="padding:0"><div class="list-ele">{Tigh(permission.name)}</div></td>
            <td>
                <button onclick="editPermission(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delPermission(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
});