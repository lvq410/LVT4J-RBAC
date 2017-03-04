function loadProducts() {
    q('/edit/product/list.json',
        {
            keyword: $('#keyword').val(),
            pager: $('#productsPager').pagerSerialize()
        },
        function(products){
            $('#products').html($tpl(tpl_products)(products));
            $('#editProductDiv').slideUp();
        },
        '加载产品中'
    );
}

function addProduct() {
    $('#editProductDiv').formData({});
    $('#adminUserId').select2Clear();
    $('#editProductDiv').slideDown().scrollToMe();
}

function editProduct(btn) {
    var product = $(btn).closest('tr').attrData();
    product.oldId = product.id;
    $('#editProductDiv').formData(product);
    $('#editProductDiv').slideDown().scrollToMe();
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
            id:product.id
        },
        function() {
            alert('删除成功!');
            loadProducts();
        },
        '删除产品中'
    );
}

$(document).ready(ready);

function ready(){
    loadProducts();
}

function tpl_products(products) {
    if(!products) return;
    for (var i = 0; i < products.length; i++) {
        var product = products[i];
        /*<tr data="{Tigh(product)}" title="{Tigh(product.des)}">
            <td>{Tigh(product.id)}</td>
            <td>{Tigh(product.name)}</td>
            <td>{Tigh(TtimestampFormat(product.lastModify))}</td>
            <td>
                <button onclick="editProduct(this)" type="button" class="btn btn-info btn-minier">编辑</button>
                <button onclick="delProduct(this)" type="button" class="btn btn-danger btn-minier">删除</button>
            </td>
        </tr>*/
    }
}