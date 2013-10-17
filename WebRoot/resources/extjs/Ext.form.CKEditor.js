/****************************************************
* CKEditor Extension
* by Maple Nan 2010.7.21
* http://witmax.cn/extjs-ckeditor.html
*****************************************************/
Ext.form.CKEditor = function(config){
    this.config = config;    
    Ext.form.CKEditor.superclass.constructor.call(this, config);
};
Ext.form.CKEditor.CKEDITOR_CONFIG = "../ckeditor/config.js";
Ext.form.CKEditor.CKEDITOR_TOOLBAR = "Default";
Ext.extend(Ext.form.CKEditor, Ext.form.TextArea,  {
    onRender : function(ct, position){
        if (!this.el) {   
                    this.defaultAutoCreate = {   
                        tag : "textarea",   
                        autocomplete : "off"  
                    };   
                }   
         Ext.form.TextArea.superclass.onRender.call(this, ct, position);   
         this.ckEditor = CKEDITOR.replace(this.id, Ext.apply({   
                                    skin : 'v2'  
                                }, this.config.CKConfig));
    },
    
    onDestroy: function(){
        if (CKEDITOR.instances[this.id]) {
            delete CKEDITOR.instances[this.id];
        }
    },
	
    setValue : function(value){
        Ext.form.TextArea.superclass.setValue.apply(this,[value]);
		CKEDITOR.instances[this.id].setData( value );
		
    },

    getValue : function(){
	    CKEDITOR.instances[this.id].updateElement();
	    var value=CKEDITOR.instances[this.id].getData();
	    Ext.form.TextArea.superclass.setValue.apply(this,[value]);
	    return Ext.form.TextArea.superclass.getValue.apply(this);
    },

    getRawValue : function(){
		CKEDITOR.instances[this.id].updateElement();	
		return Ext.form.TextArea.superclass.getRawValue(this);
    },
    
    isDirty : function() {
        if(this.disabled) {
            return false;
        }
        var value = String(this.getValue()).replace(/\s/g,'');
        value = (value == "<br />" || value == "<br/>" ? "" : value);
        this.originalValue = this.originalValue || "";
        this.originalValue = this.originalValue.replace(/\s/g,'');
        return String(value) !== String(this.originalValue) ? String(value) !== "<p>"+String(this.originalValue)+"</p>" : false;
    }
});
Ext.reg('ckeditor', Ext.form.CKEditor);