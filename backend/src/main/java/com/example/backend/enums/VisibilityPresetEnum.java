package com.example.backend.enums;

public enum VisibilityPresetEnum {
    PUBLIC(0, "全公开"),
    HIDE_CONTACT(1, "隐藏联系方式"),
    REGISTERED_ONLY(2, "仅注册用户可见全部"),
    OWNER_ONLY(3, "仅自己可见");

    private final int code;
    private final String desc;

    VisibilityPresetEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static VisibilityPresetEnum fromCode(Integer code) {
        if (code == null) return PUBLIC;
        for (VisibilityPresetEnum e : values()) {
            if (e.code == code) return e;
        }
        return PUBLIC;
    }
}