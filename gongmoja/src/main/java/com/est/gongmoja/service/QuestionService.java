package com.est.gongmoja.service;

import com.est.gongmoja.entity.QuestionEntity;

import com.est.gongmoja.entity.UserEntity;
import com.est.gongmoja.exception.ErrorCode;
import com.est.gongmoja.repository.QuestionRepository;

import com.est.gongmoja.repository.UserRepository;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    private final UserRepository userRepository; // user 정보 추가

    public List<QuestionEntity> getList() {
        return questionRepository.findAll();
    }

    public QuestionEntity getQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Page<QuestionEntity> getList(int page) {
//        List<Sort.Order> sorts = List.of(Sort.Order.desc("createDate"));
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return questionRepository.findAll(pageable);
    }

    public void create(String subject, String content, MultipartFile imageFile, UserEntity user) throws IOException {
        QuestionEntity question = new QuestionEntity();
        question.setSubject(subject);
        question.setContent(content);
        question.setUser(user);
        question.setCreateDate(LocalDateTime.now());

        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/questionFiles";

//        String filesDirectory = projectPath + "/files";
//        File filesDirectoryFile = new File(filesDirectory);
//        if (!filesDirectoryFile.exists()) {
//            filesDirectoryFile.mkdirs();
//        }

        if (imageFile != null && !imageFile.isEmpty()) {
            // 파일업로드시 userName별 디렉토리 생성
            String userDirectory = projectPath + "/" + user.getUserName();
            File userDirectoryFile = new File(userDirectory);
            if (!userDirectoryFile.exists()) {
                userDirectoryFile.mkdirs();
            }

            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + imageFile.getOriginalFilename();
            File saveFile = new File(userDirectory, fileName);

            imageFile.transferTo(saveFile);

            question.setFileName(fileName);
            question.setFilePath("/questionFiles/" + user.getUserName() + "/" + fileName); // 수정된 파일 경로
        }

        questionRepository.save(question);
    }
}