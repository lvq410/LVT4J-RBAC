$(loadProducts);

function queryProducts(){
    $('#productsPager').pagerPageNo(1);
    loadProducts()
}

function loadProducts() {
    q('/edit/product/list.json',
        {
            keyword: $('#keyword').val(),
            pager: $('#productsPager').pagerSerialize()
        },
        function(data){
            $('#productsPager').pagerCount(data.count);
            $('#products').html(tpl_products(data.models));
            if($('#editProductDiv').dialog('instance')) $('#editProductDiv').dialog('close');
        }, '加载产品中'
    );
}

function addProduct() {
    $('#editProductDiv').formData({});
    $('#adminUserId').select2Clear();
    $('#editProductDiv').dialog({
        title:'新建产品',
        minWidth:1000,
        buttons:{'保存':editProductSave}
    });
}

function sortProduct() {
    var autoIds = [];
    $('#products tr').each(function(){
        autoIds.push($(this).attrData().autoId);
    });
    if(autoIds.length==0) return alert('无排序内容!');
    if(!confirm('确定将当前内容按所展示的顺序排序吗')) return;
    q('/edit/product/sort.json',
        {
            autoIds: autoIds,
        },
        function(){
            alert('保存排序成功!');
        },
        '保存排序中'
    );
}

function editProduct(btn) {
    var product = $(btn).closest('tr').attrData();
    q('/edit/product/get.json',{autoId:product.autoId}, function(data){
        if(!data){
            loadProducts();
            return alert('产品不存在');
        }
        $('#editProductDiv').formData(data);
        $('#editProductDiv').dialog({
            title:'修改产品',
            minWidth:1000,
            buttons:{'保存':editProductSave}
        });
    }, '加载产品中');
}

function editProductSave() {
    var product = $('#editProductDiv').formData();
    if(!product) return;
    q('/edit/product/set.json',
        product,
        function() {
            alert('保存成功');
            loadProducts();
        },
        '保存产品中'
    );
}

function delProduct(btn) {
    var product = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除产品\nID:'+product.id+'\n名称:'+product.name+'\n吗?')) return;
    q('/edit/product/del.json',
        {
            autoId:product.autoId
        },
        function() {
            alert('删除成功!');
            loadProducts();
        },
        '删除产品中'
    );
}


var tpl_products = $tpl(function(products){
    if(!products) return;
    for (var i = 0; i < products.length; i++) {
        var product = products[i];
        /*<tr data="{Tigh(product)}" title="{Tigh(product.des)}">
            <td class="sortabler-handler"><i class="ace-icon fa fa-arrows-v"></i></td>
            <td style="padding:0"><div class="list-ele">{Tigh(product.id)}</div></td>
            <td style="padding:0"><div class="list-ele">{Tigh(product.name)}</div></td>
            <td style="padding:0"><div class="list-ele">{Tigh(TtimestampFormat(product.lastModify))}</div></td>
            <td>
                <button onclick="editProduct(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delProduct(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
})