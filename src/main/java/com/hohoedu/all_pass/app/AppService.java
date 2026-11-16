package com.hohoedu.all_pass.app;

import com.hohoedu.all_pass.app._dto.AppRespDTO;
import com.hohoedu.all_pass.app._dto.AppRespDTO.ClassWeekDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final com.hohoedu.all_pass.student.repository.StudentRepository studentRepository;

    public ClassAppRespDTO.BookListMainRespDTO getBookMainInfo(ClassAppReqDTO.BookListMainReqDTO dto) {

        List<ClassAppRespDTO.BookListMainRawDTO> raws = appRepository.findBookMainInfo(dto.getIhak());

        ClassAppRespDTO.BookListMainRespDTO resp = new ClassAppRespDTO.BookListMainRespDTO();

        ClassAppRespDTO.BookListMainRawDTO first = raws.get(0);
        resp.setHak_info(first.getHakInfo());
        resp.setYyyy(first.getYyyy());
        resp.setMm(first.getMm());

        // data 리스트 가공
        List<ClassAppRespDTO.BookListMainRespDTO.BookMainList> dataList = raws.stream()
                .map(row -> {
                    ClassAppRespDTO.BookListMainRespDTO.BookMainList item = new ClassAppRespDTO.BookListMainRespDTO.BookMainList();

                    item.setWeek_subject(row.getWeekSubject());
                    item.setWeek_title(row.getWeekTitle());
                    item.setWeek_publisher(row.getWeekPublisher());
                    item.setWeek_bookimg(row.getWeekBookimg());

                    return item;
                })
                .toList();

        resp.setData(dataList);

        return resp;
    }

    // 도서 상세 화면
    public ClassAppRespDTO.BookListRespDTO getBookInfo(ClassAppReqDTO.BooklistReqDTO dto) {
        String studentId = dto.getId();
        String yy = dto.getYyyy();
        String mm = dto.getMm();
        List<ClassAppRespDTO.BookListRawDTO> raws = appRepository.findBookInfo(studentId, yy, mm);

        ClassAppRespDTO.BookListRespDTO resp = new ClassAppRespDTO.BookListRespDTO();

        ClassAppRespDTO.BookListRawDTO first = raws.get(0);
        resp.setHak_info(first.getHakInfo());

        // data 리스트 가공
        List<ClassAppRespDTO.BookListRespDTO.BookList> dataList = raws.stream()
                .map(row -> {
                    ClassAppRespDTO.BookListRespDTO.BookList item = new ClassAppRespDTO.BookListRespDTO.BookList();

                    item.setWeek_subject(row.getWeekSubject());
                    item.setWeek_title(row.getWeekTitle());
                    item.setWeek_publisher(row.getWeekPublisher());
                    item.setWeek_bookimg(row.getWeekBookimg());

                    return item;
                })
                .toList();

        resp.setBooks(dataList);

        return resp;
    }

    public List<ClassWeekDTO> getClassWeek(String year, String month, String centerCode) {
        return appRepository.selectClassWeek(year, month, centerCode);
    }

    public List<AppRespDTO.QnaRespDTO> getQnaList(String gubun){
        return appRepository.findQnaList(gubun);
    }

    public List<AppRespDTO.NotiOptionRespDTO> getNotiOptions(String studentId) {
        return appRepository.findNotiOptions(studentId);
    }

    public void saveNotiOptions(com.hohoedu.all_pass.app._dto.AppReqDTO.NotiOptionSaveReqDTO dto) {
        // 형제는 앱 토큰을 같이 쓰므로(updateAppToken) 설정도 형제 모두에게 같이 저장한다 — 한 기기에 설정 하나
        java.util.Set<String> studentIds = new java.util.LinkedHashSet<>();
        studentIds.add(dto.getStuid());
        studentRepository.findSiblingByStudentId(dto.getStuid())
                .forEach(s -> studentIds.add(s.getStudentId()));

        for (var item : dto.getOptions()) {
            if (item.getTypeCode() == null) continue;
            // Y/N 외의 값은 받는 것으로 본다
            String yn = "N".equalsIgnoreCase(item.getEnabled()) ? "N" : "Y";
            for (String studentId : studentIds) {
                appRepository.upsertNotiOption(studentId, item.getTypeCode(), yn);
            }
        }
    }
}
