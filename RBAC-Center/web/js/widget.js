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
 * 排序器初始化<br>
 * 以属性widget="sortabler"标识<br>
 * 可配置的html属性:<br>
 * ⊙sortable-connect-with:默认无<br>
 * ⊙sortable-handle:默认无<br>
 * ⊙sortable-items:默认无<br>
 * ⊙sortable-cancel:默认无<br>
 */
function widget_sortabler_init() {
    $('[widget=sortabler]').each(function() {
        var widget = $(this);
        if(widget.data('widget-init')) return;
        widget.data('widget-init', true);
        var option = {};
        var connectWith = widget.attr('sortable-connect-with');
        if(connectWith!=null) option.connectWith = connectWith;
        var handle = widget.attr('sortable-handle');
        if(handle!=null) option.handle = handle;
        var items = widget.attr('sortable-items');
        if(items!=null) option.items = items;
        var cancel = widget.attr('sortable-cancel');
        if(cancel!=null) option.cancel = cancel;
        widget.sortable(option);
    });
}

/**
 * 输入框内容自动补全初始化<br>
 * 以属性widget=autocomplete标识<br>
 * 必须的html属性:<br>
 * items:自动补全的选项，可以是jsonarray字符串形式的数据，也可以是js代码但执行的返回结果是jsonarray<br>
 * multi:是否可多选，默认false
 */
function widget_autocomplete_init(){
    $('[widget=autocomplete]').each(function(){
        var widget = $(this);
        if(widget.data('widget-init')) return;
        widget.data('widget-init', true);
        var multi = widget.attr('multi')=='true';
        var config = {
            source: Tjson(widget.attr('items')),
            items: 100,
            minLength: 0,
            delay: 0
        };
        if(multi){
            config['select'] = function(event, ui){
                var terms = this.value.split(/,/);
                terms.pop();
                if(!terms.includes(ui.item.value)) terms.push(ui.item.value);
                terms.push('');
                this.value = terms.join(',');
                setTimeout(function(){widget.autocomplete('search', '')},100);
                return false;
            };
            widget.on("keydown",function(event){
                setTimeout(function(){
                    var vals = widget.val().split(/,/);
                    var keyword = vals[vals.length-1]||'';
                    widget.autocomplete('search', keyword);
                }, 100);
            });
        }
        widget.autocomplete(config);
        widget.bind('focus', function(){$(this).keydown()})
    });
}

/**
 * 时间日期选择器<br>
 * 必须是input元素,以属性widget="date-time-picker"标识<br>
 * 可配置的html属性:<br>
 * date-format:默认'yy-mm-dd'<br>
 * time-format:默认'HH:mm:ss'<br>
 * date-time-separator:默认' '<br>
 * date-only:默认false<br>
 * time-only:默认false<br>
 */
$.datepicker.setDefaults($.datepicker.regional['cn']);
function widget_dateTimePicker_init() {
    $('input[widget=date-time-picker]').each(function(){
        var widget = $(this);
        if(widget.data('widget-init')) return;
        widget.data('widget-init', true);
        var dateOnly = widget.attr('date-only')=='true';
        var initFunc = dateOnly?'datepicker':'datetimepicker';
        var dateFormat = widget.attr('date-format') || 'yy-mm-dd';
        widget[initFunc]({
            dateFormat: dateFormat,
            timeFormat: widget.attr('time-format') || 'HH:mm:ss',
            dateInput: true,
            timeInput: true,
            changeMonth: true,
            changeYear: true,
            showOtherMonths: true,
            selectOtherMonths: true,
            separator: widget.attr('date-time-separator') || ' ',
            timeOnly: widget.attr('time-only')=='true',
            prevText: '上月',
            monthNamesShort: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
            nextText: '下月',
            dayNames: ['周日','周一','周二','周三','周四','周五','周六'],
            dayNamesMin: ['日','一','二','三','四','五','六'],
            timeText: '时刻',
            hourText: '时',
            minuteText: '分',
            secondText: '秒',
            currentText: '现在',
            closeText: '确定',
            onChangeMonthYear: function(year, month, picker){
                if(!dateOnly) return;
                picker.input.val($.datepicker.formatDate(dateFormat, new Date(year, month-1, 1)));
                picker.input.change();
            }
        });
    });
}

/**
 * 选择器初始化<br>
 * 必须是select元素,以属性widget="select2"标识<br>
 * 必须的html属性:<br>
 * ⊙model:请求数据对象类型<br>
 * 可配置的html属性:<br>
 * ⊙placeholder:默认'请选择...'<br>
 * ⊙select2multiple(多选属性):默认false<br>
 * ⊙proAutoId:搜索时附加的proAutoId参数，默认''，没有则用curPro<br>
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
            multiple:widget.attr('select2multiple') == 'true',
            ajax : {
                url : '/edit/'+widget.attr('model')+'/list.json',
                dataType : 'json',
                delay: 500,
                data: function (params) {
                    var data = {
                        keyword : params.term,
                        pager : JSON.stringify({pageNo : params.page})
                    };
                    if(widget.attr('proAutoId')) data.proAutoId = widget.attr('proAutoId');
                    else if(curPro) data.proAutoId = curPro.autoId;
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
                    var objs = rst.data.models;
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
        id:obj.autoId,
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
 * 回车输入事件<br>
 * 以属性onenter={callback}标识<br>
 * 可配置的html属性:<br>
 * onenter:回车按键后调用的执行代码
 */
function widget_onEnter_init(){
    $('[onenter]').each(function(){
        var widget = $(this);
        if(widget.data('widget-onenter-init')) return;
        widget.data('widget-onenter-init', true);
        widget.bind("keyup", function(event) {
            if("Enter"!=event.key) return;
            var callback = widget.attr('onenter');
            if(!callback) return;
            eval(callback);
        });
    });
}

/**
 * 翻页器初始化<br>
 * 必须是div元素,以属性widget="pager"标识<br>
 * 必须的html属性:<br>
 * ⊙onpage:翻页后的回调脚本<br>
 * 可配置的html属性:<br>
 * ⊙show-page-count:是否显示总页数,默认false<br>
 * ⊙show-count:是否显示总条数,默认false<br>
 * ⊙show-go:是否显示跳转按钮,默认false<br>
 * ⊙show-page-size:是否显示每页个数,默认false<br>
 * ⊙page-size:每页个数,默认10<br>
 */
function widget_pager_init() {
    $('div[widget=pager]').each(function(){
        var widget = $(this);
        if(widget.data('widget-init')) return;
        var showPageCount = widget.attr('show-page-count')=='true';
        var showCount = widget.attr('show-count')=='true';
        var showGo = widget.attr('show-go')=='true';
        var showPageSize = widget.attr('show-page-size')=='true';
        var pageSize = LVT.int(widget.attr('page-size'), 10);
        var pagerDiv = $('<div widget="pager">'+
                '<div class="pager-ele pager-btn pager-prev">←</div>'+
                '<div class="pager-ele">第<span class="pager-page-no input" contenteditable="true">1</span>'
                    +'<span'+(showPageCount?'':' style="display:none;"')+' class="pager-page-count-box">'
                        +'/<span class="pager-page-count">1</span></span>'
                    +'页'
                    +'<span'+(showCount?'':' style="display:none;"')+' class="pager-count-box">'
                        +'，共<span class="pager-count">0</span>条</span>'
                    +'</div>'+
                '<div'+(showGo?'':' style="display:none;"')+' class="pager-ele pager-btn pager-go">go</div>'+
                '<div'+(showPageSize?'':' style="display:none;"')+' class="pager-ele">'
                    +'每页<span class="pager-page-size input" contenteditable="true">'+pageSize+'</span>个</div>'+
                '<div class="pager-ele pager-btn pager-next">→</div>'+
            '</div>');
        pagerDiv.attr('id', widget.attr('id'));
        pagerDiv.attr('onpage', widget.attr('onpage'));
        pagerDiv.attr('class', (widget.attr('class')||'')+' pager');
        pagerDiv.attr('style', widget.attr('style'));
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
            LVT.int(pageNoEle.text(), 1)
            :LVT.int(pageNo, 1);
    if(pageNo<1) pageNo=1;
    pageNoEle.text(pageNo);
    return pageNo;
};
/** 翻页器扩展：设置或获取每页大小 */
jQuery.fn.pagerPageSize = function(pageSize){
    var pagerDiv = this.closest('[widget=pager]');
    var pageSizeEle = pagerDiv.find('.pager-page-size');
    var defPageSize = LVT.int(pagerDiv.attr('page-size'), 10);
    pageSize = pageSize==null?
            LVT.int(pageSizeEle.text(), defPageSize)
            :LVT.int(pageSize, defPageSize);
    if(pageSize<1) pageSize = 10;
    pageSizeEle.text(pageSize);
    return pageSize;
};
/** 翻页器扩展：页数及每页大小序列化为json对象 */
jQuery.fn.pagerSerialize = function(){
    return {
        pageNo   : this.pagerPageNo(),
        pageSize : this.pagerPageSize()
    }
}
/** 翻页器扩展：获取总页数 */
jQuery.fn.pagerPageCount = function(){
    var pagerDiv = this.closest('[widget=pager]');
    pageCountEle = pagerDiv.find('.pager-page-count');
    return LVT.int(pageCountEle.text());
}
/** 翻页器扩展：设置与获取总条数 */
jQuery.fn.pagerCount = function(count){
    var pagerDiv = this.closest('[widget=pager]');
    countEle = pagerDiv.find('.pager-count');
    if(count==null) return LVT.int(countEle.text());
    count = LVT.int(count);
    countEle.text(count);
    var pagerPageCount = Math.max(1, Math.ceil(count/this.pagerPageSize()));
    pageCountEle = pagerDiv.find('.pager-page-count');
    pageCountEle.text(pagerPageCount);
}

/**
 * 权限选择器初始化
 * 必须是div元素,以属性widget="auth-chooser"标识<br>
 * 必须的html属性:<br>
 * ⊙auth-model:权限类型<br>
 */
function widget_auth_chooser_init() {
    $('[widget=auth-chooser]').each(function() {
        var widget = $(this);
        if(widget.data('widget-init')) return;
        var authModel = widget.attr('auth-model');
        var chooserDiv = 
        $('<div class="auth-chooser-box col-xs-12" style="border:1px solid #d5d5d5;padding:0;">'
            +'<div class="col-xs-8 auth-chooser-rst" style="border-right:1px solid #d5d5d5;">'
                +'<input type="text" class="form-control q-auth-search" onkeyup="widge_auth_search(this)" onchange="widge_auth_search(this)" style="height:22px;margin-top:8px;" placeholder="搜索">'
            +'</div>'
            +'<div class="col-xs-4" style="border-left:1px solid #d5d5d5;min-height:80px;margin-left:-1px;">'
                +'<div class="input-group" style="width:100%;">'
                    +'<input type="text" onenter="widget_auth_load(this, &quot;'+authModel+'&quot;)" class="form-control q-auth-keyword" style="height:22px;margin-top:8px;" placeholder="请输入关键词"/>'
                    +'<span class="input-group-btn">'
                        +'<button type="button" class="btn btn-purple btn-minier" onclick="widget_auth_load(this, &quot;'+authModel+'&quot;)" style="margin-top:8px;">'
                            +'搜索<i class="ace-icon fa fa-search"></i>'
                        +'</button>'
                    +'</span>'
                +'</div>'
                +'<div class="pull-right q-auths-pager" widget="pager" show-go="true" show-page-count="true" show-count="true" onpage="widget_auth_load(this, &quot;'+authModel+'&quot;)"></div>'
                +'<div class="q-auths" style="border-top:1px solid #d5d5d5;margin-top:50px;padding-top:10px;margin-bottom:10px;"></div>'
            +'</div>'
        +'</div>');
        widget.attr('widget', 'sortabler');
        widget.addClass('auth-chooser-rst-box');
        widget.before(chooserDiv);
        chooserDiv.find('.auth-chooser-rst').append(widget);
        chooserDiv.find('button').click();
        widget_sortabler_init();
        widget_pager_init();
        chooserDiv.data('widget-init', true);
    });
    widget_onEnter_init();
}
function widget_auth_load(btn, authModelName) {
    if(!curPro) return;
    var box = $(btn).closest('.auth-chooser-box');
    q('/edit/'+authModelName+'/list.json',
        {
            proAutoId: curPro.autoId,
            keyword: box.find('.q-auth-keyword').val(),
            pager: box.find('.q-auths-pager').pagerSerialize()
        },
        function(data){
            box.find('.q-auths-pager').pagerCount(data.count);
            box.find('.q-auths').html(tpl_auths(data.models, 2));
        },
        '加载权限中'
    );
}
function widget_auth_choose(authBadge) {
    var chooserDiv = $(authBadge).closest('.auth-chooser-box');
    var chooserRst = chooserDiv.find('.auth-chooser-rst-box');
    var choosedAuths = chooserRst.formData();
    var auth = $(authBadge).attrData();
    if(Tarr.contains(choosedAuths, auth.autoId)) return;
    chooserRst.append(tpl_auths([auth], 1));
    if(window.onAuthChange) onAuthChange();
}
function widge_auth_search(input) {
    var chooser = $(input).next();
    var keyword = $(input).val();
    chooser.find('.badge').each(function(){
        var authBadge = $(this);
        if(!keyword) authBadge.show();
        var auth = authBadge.attrData();
        if(auth.id && auth.id.indexOf(keyword)!=-1) return authBadge.show();
        if(auth.pattern && auth.pattern.indexOf(keyword)!=-1) return authBadge.show();
        if(auth.name && auth.name.indexOf(keyword)!=-1) return authBadge.show();
        authBadge.hide();
    });
}

/** 初始化所有部件 */
function widget_init() {
    widget_tooltiper_init();
    widget_sortabler_init();
    widget_autocomplete_init();
    widget_dateTimePicker_init();
    widget_select2_init();
    widget_onEnter_init();
    widget_pager_init();
    widget_auth_chooser_init();
}

$(document).ready(widget_init);