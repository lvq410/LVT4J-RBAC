function loadParams(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/param/list.json',
        {
            proAutoId: curPro.autoId
        },
        function(params){
            $('#params').html($tpl(tpl_params)(params));
            $('#editParamDiv').slideUp();
            $('#paramsDiv').slideDown();
        },
        '加载配置项中'
    );
}

function addParam() {
    $('#editParamDiv').formData({});
    $('#editParamDiv').slideDown().scrollToMe();
}

function sortParam() {
    var autoIds = [];
    $('#params tr').each(function(){
        autoIds.push($(this).attrData().autoId);
    });
    if(autoIds.length==0) return alert('无排序内容!');
    q('/edit/param/sort.json',
        {
            autoIds: autoIds,
        },
        function(){
            alert('保存排序成功!');
        },
        '保存排序中'
    );
}

function editParam(btn) {
    var param = $(btn).closest('tr').attrData();
    param.oldKey = param.key;
    $('#editParamDiv').formData(param);
    $('#editParamDiv').slideDown().scrollToMe();
}

function editParamSave() {
    var param = $('#editParamDiv').formData();
    if(!param) return;
    param.proAutoId = curPro.autoId;
    q('/edit/param/set.json',
        param,
        function() {
            alert('保存成功!');
            loadParams();
        },
        '保存配置项中'
    );
}

function delParam(btn) {
    var param = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除配置项\nkey:'+param.key+'\n名称:'+param.name+'\n吗?')) return;
    q('/edit/param/del.json',
        {
            autoId:param.autoId
        },
        function(){
            alert('删除成功!');
            loadParams();
        },
        '删除配置项中'
    );
}

$(document).ready(ready);

function ready(){
    loadParams();
}

function tpl_params(params) {
    if(!params) return;
    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        /*<tr data="{Tigh(param)}" title="{Tigh(param.des)}">
            <td class="sortabler-handler"><i class="ace-icon fa fa-arrows-v"></i></td>
            <td><div class="list-ele">{Tigh(param.key)}</div></td>
            <td><div class="list-ele">{Tigh(param.name)}</div></td>
            <td>
                <button onclick="editParam(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delParam(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}