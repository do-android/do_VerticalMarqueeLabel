package doext.module.do_VerticalMarqueeLabel.define;

import core.object.DoUIModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;


public abstract class do_VerticalMarqueeLabel_MAbstract extends DoUIModule{

	protected do_VerticalMarqueeLabel_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception{
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("direction", PropertyDataType.String, "up", true));
		this.registProperty(new DoProperty("fontColor", PropertyDataType.String, "000000FF", false));
		this.registProperty(new DoProperty("fontSize", PropertyDataType.Number, "17", false));
		this.registProperty(new DoProperty("fontStyle", PropertyDataType.String, "normal", false));
		this.registProperty(new DoProperty("text", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("textFlag", PropertyDataType.String, "normal", true));
		this.registProperty(new DoProperty("style", PropertyDataType.Number, "0", true));
		this.registProperty(new DoProperty("duration", PropertyDataType.Number, "2000", true));
	}
}