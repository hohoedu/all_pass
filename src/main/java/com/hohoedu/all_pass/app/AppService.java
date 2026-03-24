package com.hohoedu.all_pass.app;

import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;

    public ClassAppRespDTO.BookListMainRespDTO getBookMainInfo(ClassAppReqDTO.BookListMainReqDTO dto) {

        List<ClassAppRespDTO.BookListMainRawDTO> raws = appRepository.findBookMainInfo(dto.getIhak());

        ClassAppRespDTO.BookListMainRespDTO resp = new ClassAppRespDTO.BookListMainRespDTO();

        ClassAppRespDTO.BookListMainRawDTO first = raws.get(0);
        resp.setHak_info(first.getHakInfo());
        resp.setYyyy(first.getYyyy());
        resp.setMm(first.getMm());

        // data 리스트 가공
        List<ClassAppRespDTO.BookListMainRespDTO.BookMainList> dataList =
                raws.stream()
                        .map(row -> {
                            ClassAppRespDTO.BookListMainRespDTO.BookMainList item =
                                    new ClassAppRespDTO.BookListMainRespDTO.BookMainList();

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
        List<ClassAppRespDTO.BookListRespDTO.BookList> dataList =
                raws.stream()
                        .map(row -> {
                            ClassAppRespDTO.BookListRespDTO.BookList item =
                                    new ClassAppRespDTO.BookListRespDTO.BookList();

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




}
