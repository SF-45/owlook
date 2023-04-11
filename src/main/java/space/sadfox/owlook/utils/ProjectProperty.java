package space.sadfox.owlook.utils;

public enum ProjectProperty {
    PATH_TO_XML(""),
    PATH_TO_TABLE_CONFIG(ProjectPath.CONFiG.getPath().resolve("tables_config.xml").toString()),
	BOOT_PATHES(ProjectPath.CONFiG.getPath().resolve("boot").toString());


    private String value;
    private String defaultValue;

    ProjectProperty(String defaultValue) {
        this.defaultValue = defaultValue;
        this.value = PropertyAccessor.getProperty(this);
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setValue(String value) {
        PropertyAccessor.setProperty(this, value);
        this.value = value;
    }
}
