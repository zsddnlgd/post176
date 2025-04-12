package com.yangxianwen.post176.values;

public class Constants {

    public static final String defAdminPassword = "syp427";

    // 学生未找到
    public static final int STUDENT_NOT_FOUND = 1001;
    public static final String STUDENT_NOT_FOUND_MESSAGE = "学生未找到。";

    // NFC ID已经绑定到其他人
    public static final int NFC_ID_ALREADY_BOUND = 1002;
    public static final String NFC_ID_ALREADY_BOUND_MESSAGE = "此nfcId已经绑定到其他人，不能重新绑定";

    // nfcId更新成功
    public static final int NFC_ID_UPDATE_SUCCESS = 0;
    public static final String NFC_ID_UPDATE_SUCCESS_MESSAGE = "nfcId更新成功";

    // 请求数据不能为空
    public static final int REQUEST_DATA_EMPTY = 1003;
    public static final String REQUEST_DATA_EMPTY_MESSAGE = "请求数据不能为空";

    // 订单已存在
    public static final int ORDER_EXISTS = 1004;
    public static final String ORDER_EXISTS_MESSAGE = "订单信息已存在，无法重复创建";

    // 数据库更新失败
    public static final int DATABASE_UPDATE_ERROR = 1005;
    public static final String DATABASE_UPDATE_ERROR_MESSAGE = "数据库更新失败";

    // 未知错误
    public static final int UNKNOWN_ERROR = 1006;
    public static final String UNKNOWN_ERROR_MESSAGE = "发生未知错误，请稍后重试";
}
