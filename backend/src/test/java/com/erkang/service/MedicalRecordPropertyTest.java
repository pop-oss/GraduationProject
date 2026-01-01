package com.erkang.service;

import com.erkang.common.BusinessException;
import com.erkang.domain.entity.MedicalRecord;
import com.erkang.mapper.MedicalAttachmentMapper;
import com.erkang.mapper.MedicalRecordMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 病历不可变性属性测试
 * **Property 7: 医疗记录不可变性**
 * **Validates: Requirements 5.6, 7.6**
 */
class MedicalRecordPropertyTest {

    /**
     * Property 7.1: 已提交的病历不允许修改
     * *For any* submitted medical record, update operations should be rejected
     */
    @Property(tries = 100)
    void submittedRecordShouldNotBeModifiable(
            @ForAll @LongRange(min = 1, max = 1000000) Long recordId,
            @ForAll("medicalRecordUpdates") MedicalRecord updates) {
        
        MedicalRecordMapper recordMapper = Mockito.mock(MedicalRecordMapper.class);
        MedicalAttachmentMapper attachmentMapper = Mockito.mock(MedicalAttachmentMapper.class);
        MedicalRecordService service = new MedicalRecordService(recordMapper, attachmentMapper);
        
        // 模拟已提交的病历
        MedicalRecord submittedRecord = new MedicalRecord();
        submittedRecord.setId(recordId);
        submittedRecord.setStatus("SUBMITTED");
        submittedRecord.setChiefComplaint("原主诉");
        submittedRecord.setDiagnosis("原诊断");
        
        when(recordMapper.selectById(recordId)).thenReturn(submittedRecord);
        
        // 尝试修改应该抛出异常
        assertThatThrownBy(() -> service.updateRecord(recordId, updates))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("已提交的病历不允许修改");
    }

    /**
     * Property 7.2: 病历不允许删除
     * *For any* medical record, delete operations should always be rejected
     */
    @Property(tries = 100)
    void medicalRecordShouldNotBeDeletable(
            @ForAll @LongRange(min = 1, max = 1000000) Long recordId) {
        
        MedicalRecordMapper recordMapper = Mockito.mock(MedicalRecordMapper.class);
        MedicalAttachmentMapper attachmentMapper = Mockito.mock(MedicalAttachmentMapper.class);
        MedicalRecordService service = new MedicalRecordService(recordMapper, attachmentMapper);
        
        // 删除操作应该始终被拒绝
        assertThatThrownBy(() -> service.deleteRecord(recordId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("病历不允许删除");
    }

    /**
     * Property 7.3: 草稿状态的病历可以修改
     * *For any* draft medical record, update operations should succeed
     */
    @Property(tries = 100)
    void draftRecordShouldBeModifiable(
            @ForAll @LongRange(min = 1, max = 1000000) Long recordId,
            @ForAll @StringLength(min = 1, max = 100) String newChiefComplaint) {
        
        MedicalRecordMapper recordMapper = Mockito.mock(MedicalRecordMapper.class);
        MedicalAttachmentMapper attachmentMapper = Mockito.mock(MedicalAttachmentMapper.class);
        MedicalRecordService service = new MedicalRecordService(recordMapper, attachmentMapper);
        
        // 模拟草稿状态的病历
        MedicalRecord draftRecord = new MedicalRecord();
        draftRecord.setId(recordId);
        draftRecord.setStatus("DRAFT");
        draftRecord.setChiefComplaint("原主诉");
        
        when(recordMapper.selectById(recordId)).thenReturn(draftRecord);
        when(recordMapper.updateById(any())).thenReturn(1);
        
        // 更新操作
        MedicalRecord updates = new MedicalRecord();
        updates.setChiefComplaint(newChiefComplaint);
        
        MedicalRecord result = service.updateRecord(recordId, updates);
        
        // 验证更新成功
        assertThat(result.getChiefComplaint()).isEqualTo(newChiefComplaint);
    }

    /**
     * Property 7.4: 提交病历需要必填字段
     * *For any* medical record without required fields, submit should fail
     */
    @Property(tries = 100)
    void submitShouldRequireMandatoryFields(
            @ForAll @LongRange(min = 1, max = 1000000) Long recordId,
            @ForAll("incompleteRecords") MedicalRecord incompleteRecord) {
        
        MedicalRecordMapper recordMapper = Mockito.mock(MedicalRecordMapper.class);
        MedicalAttachmentMapper attachmentMapper = Mockito.mock(MedicalAttachmentMapper.class);
        MedicalRecordService service = new MedicalRecordService(recordMapper, attachmentMapper);
        
        incompleteRecord.setId(recordId);
        incompleteRecord.setStatus("DRAFT");
        
        when(recordMapper.selectById(recordId)).thenReturn(incompleteRecord);
        
        // 提交不完整的病历应该失败
        assertThatThrownBy(() -> service.submitRecord(recordId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("必填项");
    }

    @Provide
    Arbitrary<MedicalRecord> medicalRecordUpdates() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(100)
            .map(s -> {
                MedicalRecord record = new MedicalRecord();
                record.setChiefComplaint(s);
                record.setDiagnosis(s);
                return record;
            });
    }

    @Provide
    Arbitrary<MedicalRecord> incompleteRecords() {
        return Arbitraries.of(
            createIncompleteRecord(null, null),
            createIncompleteRecord("主诉", null),
            createIncompleteRecord(null, "诊断")
        );
    }

    private MedicalRecord createIncompleteRecord(String chiefComplaint, String diagnosis) {
        MedicalRecord record = new MedicalRecord();
        record.setChiefComplaint(chiefComplaint);
        record.setDiagnosis(diagnosis);
        return record;
    }
}
