package atos.mae.auto.model;


public class WebElementModelList {

	/**
	 * Page's List or Section list.
	 */
	public WebElementModelList Base[];


	/**
	 * Section's Name.
	 */
	public String SectionName;

	/**
	 * List of identifier.
	 */
	public WebElementModel List[];

	public void defaultObjectRepository(boolean isBase){
		if(isBase){
			final WebElementModelList list = new WebElementModelList();
			list.defaultObjectRepository(false);
			this.Base = new WebElementModelList[1];
			this.Base[0] = list;
		}else{
			this.SectionName="Home_Page";

			final WebElementModel IM1 = new WebElementModel();
			IM1.setObjectName("btn_Search");
			IM1.setXPath("//div[@id='formulaire_recherche']/form/button");
			final WebElementModel IM2 = new WebElementModel();
			IM2.setObjectName("txtbx_Search");
			IM2.setXPath("//input[@id='recherche']");

			this.List = new WebElementModel[2];
			this.List[0] = IM1;
			this.List[1] = IM2;
		}
	}

}
