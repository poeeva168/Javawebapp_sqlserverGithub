
Ext.ux.TabCloseMenuPlug = function(){
    var tabs, menu, ctxItem;
    this.init = function(tp){
        tabs = tp;
        tabs.on('contextmenu', onContextMenu);
    };

    function onContextMenu(ts, item, e){
      
        
    	if(!menu){ // create context menu on first right click
            menu = new Ext.menu.Menu({
            
            iid:item.id,	
            items: [
            	{
            		icon:'icons/arrow_circle.png',
	                id: tabs.id + '-ref',
	                text: '重新载入',
	                handler :function()
	                {
	                	 var panel=Ext.getCmp(menu.iid);
	                	 panel.reload();
	                }
            	},
            	{
            		icon:'icons/arrow_circle_double.png',
	                id: tabs.id + '-ref-new',
	                text: '刷新页面',
	                handler :function()
	                {
	                	 var panel=Ext.getCmp(menu.iid);
	                	 panel.reload(true);
	                }
            	},
            	{
	                icon:'icons/application_side_contract.png',
	                id: tabs.id + '-mu',
	                text: '左侧菜单',
	                handler : function(){
	                   switchTree();
	                }
            	},
            	{
	                icon:'icons/application.png',
            		id: tabs.id + '-wk',
	                text: '最大化',
	                handler : function(){
	                   switchWindow();
	                }
            	},
            	{
                id: tabs.id + '-close',
                text: '关闭标签页',
                handler : function(){
                	// tabs.remove(ctxItem);
                	// ctxItem.destroy();
                	// tabs.checkCloseButton();
                	
                	tabs.closeTab(ctxItem)
                	
                }
            },{
                id: tabs.id + '-close-others',
                text: '关闭其他标签页',
                handler : function(){
                	
                    tabs.closeOthers(ctxItem);
                    
                }
            },
            
            
            {
                id: tabs.id + '-show-url',
                text: '显示地址',
                handler : function(){
                	
                    var panel=Ext.getCmp(menu.iid);
                    if(panel.getFrame())
                    {
                    	App.showPageUrl(panel.getFrame().location);
                    }
                    
                    
                }
            },
            {
                id: tabs.id + '-show-src',
                text: '源代码',
                handler : function(){
                	
                    var panel=Ext.getCmp(menu.iid);
                    if(panel.getFrame())
                    {
                    	App.viewSource(panel.getFrame());
                    }
                }
            }
            
            
            ]});
        }
        else
        {
        	menu.iid=item.id;
        }

        ctxItem = item;
        var items = menu.items;

        
        var disableOthers = true;
        tabs.items.each(function(){
            if(this != item && this.closable){
                disableOthers = false;
                return false;
            }
        });
        
        // items.get(tabs.id + '-close').setDisabled(disableOthers);
        // items.get(tabs.id + '-close-others').setDisabled(disableOthers);
        
        items.get(tabs.id + '-close').setVisible(!disableOthers);
        items.get(tabs.id + '-close-others').setVisible(!disableOthers);
        
        items.get(tabs.id + '-mu').setVisible(WIN_STATE!="max");
        items.get(tabs.id + '-wk').setText(WIN_STATE=="max"?'还原工作区':'最大化工作区');
        
        
        
        var p=Ext.getCmp(menu.iid);
        var f=p.getFrame();
        var z1=f.location.href.indexOf(p.absUrl);
        var z2=f.location.href.length-p.absUrl.length
        if(z1==z2)
        {
        	items.get(tabs.id + '-ref-new').setVisible(false);
        }
        else
        {
        	items.get(tabs.id + '-ref-new').setVisible(true);
        }
        
        menu.doLayout();
        
		e.stopEvent();
        menu.showAt(e.getPoint());
    }
};

Ext.preg('tabclosemenuplug', Ext.ux.TabCloseMenuPlug);