function loadParams(){
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/param/list.json',
        {
            proId: curPro.id
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
    $('#editParamDiv').slideDown();
}

function editParam(btn) {
    var param = $(btn).closest('tr').attrData();
    param.oldKey = param.key;
    $('#editParamDiv').formData(param);
    $('#editParamDiv').slideDown();
}

function editParamSave() {
    var param = $('#editParamDiv').formData();
    if(!param) return;
    param.proId = curPro.id;
    q('/edit/param/set.json',
        param,
        loadParams,
        '保存配置项中'
    );
}

function delParam(btn) {
    var param = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除配置项\nkey:'+param.key+'\n名称:'+param.name+'\n吗?')) return;
    q('/edit/param/del.json',
        {
            proId:curPro.id,
            key:param.key
        },
        loadParams,
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
            <td>{Tigh(param.key)}</td>
            <td>{Tigh(param.name)}</td>
            <td>
                <button onclick="editParam(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delParam(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}