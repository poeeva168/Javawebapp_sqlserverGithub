/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function(config) {
	// Define changes to default configuration here. For example:
	config.language = 'zh-cn'; // 配置语言
	//config.uiColor = '#FFF'; // 背景颜色
	config.width = 'auto'; // 宽度
	//config.height = 'auto'; // 高度
	config.skin = 'v2';//界面v2,kama,office2003
	config.toolbar = 'Full';// 工具栏风格Full,Basic
	
	
	/*
	config.filebrowserBrowseUrl = '/jwgl_oracle/ckeditor_browser?Type=File';
    config.filebrowserImageBrowseUrl = 'ckfinder/ckfinder.html?Type=Images';  
    config.filebrowserFlashBrowseUrl = 'ckfinder/ckfinder.html?Type=Flash';
   */
	
	config.filebrowserUploadUrl='/Javawebapp_sqlserver/ckeditor_uploader?Type=File';
	config.filebrowserImageUploadUrl='/Javawebapp_sqlserver/ckeditor_uploader?Type=Image';
	config.filebrowserFlashUploadUrl='/Javawebapp_sqlserver/ckeditor_uploader?Type=Flash';
	 
	
};
