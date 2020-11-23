package com.yyj.stydyroom.study.util.enums;

public enum  RoomType {
    General("通用",1),
    Chinese("语文",2),
    Math("数学",3),
    English("英语",4),
    ;

    String typeName;
    int typeId;

    RoomType(String typeName, int typeId) {
        this.typeName = typeName;
        this.typeId = typeId;
    }

    public static String getTypeByTypeId(int typeId){
        for (RoomType roomType:RoomType.values()){
            if (roomType.typeId == typeId)
                return roomType.typeName;
        }
        return General.typeName;
    }

    public static int getTypeIdByType(String typeName){
        for (RoomType roomType:RoomType.values()){
            if (typeName.equals(roomType.typeName))
                return roomType.typeId;
        }
        return General.typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTypeId() {
        return typeId;
    }
}
