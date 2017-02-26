/**
 * 各类控件
 */

/**
 * 初始化tooltip
 */
function widget_tooltiper_init(){
    $(document).tooltip({
        items: '[title], .msg-tooltiper',
        trigger: 'hover focus blur click',
        content: function(){
            var ele = $(this);
            if(ele.is('[title]')) return Tigh(ele.attr('title')).replace(/\n/g, '<br>');
            if(ele.is('.msg-tooltiper'))
                return $('<div>').addClass('text-info').html(ele.find('.tooltip-msg:first').html()).wrap('<div>').html();
        }
    });
}



/**
 * 选择器初始化<br>
 * 必须是select元素,以属性widget="select2"标识<br>
 * 必须的html属性:<br>
 * ⊙url:请求数据地址<br>
 * 可配置的html属性:<br>
 * ⊙placeholder:默认'请选择...'<br>
 */
function widget_select2_init() {
    $('[widget=select2]').each(function(){
        var widget = $(this);
        if(widget.data('widget-init')) return;
        widget.data('widget-init', true);
        var url = widget.attr('url');
        var placeHolder = widget.attr('placeholder');
        if(!placeHolder) placeHolder = '请选择...';
        var allowClear = widget.attr('allow-clear')=='true';
        widget.select2({
            placeholder : placeHolder,
            allowClear : allowClear,
            templateResult : function(rst){
                if(rst.loading) return '加载中...';
                var obj = rst.data;
                return $($tpl(widget_tpl_select)(obj));
            },
            ajax : {
                url : url,
                dataType : 'json',
                delay: 500,
                data: function (params) {
                    var data = {
                        keyword : params.term,
                        pager : JSON.stringify({pageNo : params.page})
                    };
                    if(curPro) data.proId = curPro.id;
                    return data;
                },
                processResults: function (rst, params) {
                    console.info(placeHolder+'查询结果', rst);
                    params.page = params.page || 1;
                    if(rst.err!=0){
                        alert('error:'+rst.err+'\nmsg:'+rst.msg);
                        return {
                            results: [],
                            pagination: {more: false}
                        }
                    }
                    var objs = rst.data;
                    var results = [];
                    for (var i = 0; i < objs.length; i++) {
                        var obj = objs[i];
                        results.push(widget_select2Result(obj));
                    }
                    return {
                        results: results,
                        pagination: {
                            more: results.length==10
                        }
                    };
                },
            }
        });
        widget.on('change', function(){
            var results = widget.data('select2').data();
            widget.data('select2:data', results.length==0?null:results[0].data);
        });
        widget.on('select2:close', function(){
            $('[role="tooltip"]').remove();
        });
    });
};
function widget_select2Result(obj){
    return {
        id:Tfnn(obj.id, obj.pattern),
        text:obj.name,
        data:obj
    };
};
function widget_tpl_select(obj){
    /*<div class="msg-tooltiper">
        {Tigh(obj.name)}
        <div class="tooltip-msg">
            <strong>{obj.id?'ID:':(obj.pattern?'Pattern:':'')}</strong>{Tigh(Tfnn(obj.id, obj.pattern))}<br>
            {Tigh(obj.des).replace(/\n/g, '<br>')}
        </div>
    </div>*/
}
/** select2相关jquery扩展:清除select2数据 */
jQuery.fn.select2Clear = function(){
    this.empty();
    this.val(null);
    this.data('select2:data', null);
    this.trigger('change');
};
/** select2相关jquery扩展:取消select2选择 */
jQuery.fn.select2Deselect = function(){
    this.val(null);
    this.data('select2:data', null);
    this.trigger('change');
};
/** select2相关jquery扩展:设置select2数据 */
jQuery.fn.select2Set = function(data){
    this.empty();
    var result = widget_select2Result(data);
    var option = $('<option/>');
    option.attr('value', result.id);
    option.text(result.text);
    this.append(option);
    this.removeAttr("title");
    this.val(result.id);
    this.data('select2:data', data);
    this.trigger('change');
};

/**
 * 翻页器初始化<br>
 * 以属性widget="pager"标识<br>
 * 必须的html属性:<br>
 * ⊙onpage:翻页后的回调脚本<br>
 * 可配置的html属性:<br>
 * ⊙show-page-size:是否显示每页个数,默认是<br>
 * ⊙page-size:每页个数,默认10<br>
 */
function widget_pager_init() {
    $('[widget=pager]').each(function(){
        var widget = $(this);
        if(widget.data('widget-init')) return;
        var showPageSize = widget.attr('show-page-size');
        if(!showPageSize) showPageSize = true;
        var pageSize = LVT.int(widget.attr('page-size'), 10);
        var pagerDiv = $('<div widget="pager">'+
                '<div class="pager-ele pager-btn pager-prev">←</div>'+
                '<div class="pager-ele">第<input class="pager-page-no" type="text" value="1">页</div>'+
                '<div class="pager-ele pager-btn pager-go">go</div>'+
                '<div'+(showPageSize?'':' style="display:none;"')+' class="pager-ele">'
                    +'每页<input class="pager-page-size" type="text" value="'+pageSize+'">个</div>'+
                '<div class="pager-ele pager-btn pager-next">→</div>'+
            '</div>');
        pagerDiv.attr('id', widget.attr('id'));
        pagerDiv.attr('onpage', widget.attr('onpage'));
        pagerDiv.attr('class', widget.attr('class')+' pager');
        pagerDiv.find('.pager-prev').click(function(){
            var pagerDiv = $(this).closest('[widget=pager]');
            pagerDiv.pagerPageNo(pagerDiv.pagerPageNo()-1);
            var onpage = pagerDiv.attr('onpage');
            if(onpage) eval(onpage);
        });
        pagerDiv.find('.pager-go').click(function(){
            var pagerDiv = $(this).closest('[widget=pager]');
            var onpage = pagerDiv.attr('onpage');
            if(onpage) eval(onpage);
        });
        pagerDiv.find('.pager-next').click(function(){
            var pagerDiv = $(this).closest('[widget=pager]');
            pagerDiv.pagerPageNo(pagerDiv.pagerPageNo()+1);
            var onpage = pagerDiv.attr('onpage');
            if(onpage) eval(onpage);
        });
        widget.replaceWith(pagerDiv);
        pagerDiv.data('widget-init', true);
    });
}
/** 翻页器扩展：设置或获取当前页数 */
jQuery.fn.pagerPageNo = function(pageNo){
    var pagerDiv = this.closest('[widget=pager]');
    var pageNoEle = pagerDiv.find('.pager-page-no');
    pageNo = pageNo==null?
            LVT.int(pageNoEle.val(), 1)
            :LVT.int(pageNo, 1);
    if(pageNo<1) pageNo=1;
    pageNoEle.val(pageNo);
    return pageNo;
};
/** 翻页器扩展：设置或获取每页大小 */
jQuery.fn.pagerPageSize = function(pageSize){
    var pagerDiv = this.closest('[widget=pager]');
    var pageSizeEle = pagerDiv.find('.pager-page-size');
    var defPageSize = LVT.int(pagerDiv.attr('page-size'), 10);
    pageSize = pageSize==null?
            LVT.int(pageSizeEle.val(), defPageSize)
            :LVT.int(pageSize, defPageSize);
    if(pageSize<1) pageSize = 10;
    pageSizeEle.val(pageSize);
    return pageSize;
};
/** 翻页器扩展：页数及每页大小序列化为json对象 */
jQuery.fn.pagerSerialize = function(){
    return {
        pageNo   : this.pagerPageNo(),
        pageSize : this.pagerPageSize()
    }
}

/** 初始化所有部件 */
function widget_init() {
    widget_tooltiper_init();
    widget_select2_init();
    widget_pager_init();
}

$(document).ready(widget_init);