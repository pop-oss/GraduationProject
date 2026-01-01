package com.erkang.security;

/**
 * 数据范围类型枚举
 */
public enum DataScopeType {
    
    /** 患者仅本人数据 */
    PATIENT_SELF,
    
    /** 医生仅接诊范围数据 */
    DOCTOR_CONSULT,
    
    /** 药师仅待审处方数据 */
    PHARMACIST_REVIEW,
    
    /** 管理员全部数据 */
    ADMIN_ALL
}
