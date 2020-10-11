package org.studyproject.metagram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.studyproject.metagram.controller.ControllerUtils;
import org.studyproject.metagram.domain.Message;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.domain.dto.MessageDto;
import org.studyproject.metagram.repos.MessageRepo;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {
    private final MessageRepo messageRepo;
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public MessageService(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    /**
     * addMessage(@RequestParam("file") MultipartFile file,
     *
     * @AuthenticationPrincipal User user,
     * Model model,
     * @Valid Message message,
     * BindingResult bindingResult,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
     * adding message to database
     */
    public void addMessage(@RequestParam("file") MultipartFile file,
                           @AuthenticationPrincipal User user,
                           Model model,
                           @Valid Message message,
                           BindingResult bindingResult,
                           @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) throws IOException {
        message.setAuthor(user);
        if (file.isEmpty() || bindingResult.hasErrors()) {
            if (file.isEmpty()) {
                model.addAttribute("filenameError", "Please, add photo.");
            }
            if (bindingResult.hasErrors()) {
                Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
                model.mergeAttributes(errorsMap);
            }
        } else {
            saveFile(file, message);
            messageRepo.save(message);
        }

        Page<MessageDto> page = messageRepo.findAll(pageable, user);
        model.addAttribute("pageCount", page.getTotalPages());
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("page", page);
    }

    /**
     * public void saveFile(@RequestParam("file") MultipartFile file,
     *
     * @Valid Message message)
     * saving file to filesystem
     */
    public void saveFile(@RequestParam("file") MultipartFile file, @Valid Message message) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFilename));

            message.setFilename(resultFilename);
        }
    }

    /**
     * public Page<MessageDto> messageList(String filter, Pageable pageable, User user)
     * List of all messages in page view
     */
    public Page<MessageDto> messageList(String filter, Pageable pageable, User user) {
        if (filter != null && !filter.isEmpty()) {
            return messageRepo.findByTag(filter, pageable, user);
        } else {
            return messageRepo.findAll(pageable, user);
        }
    }

    /**
     * public Page<MessageDto> messageListForUser(Pageable pageable, User currentUser, User author)
     * List of one user messages in page view
     */
    public Page<MessageDto> messageListForUser(Pageable pageable, User currentUser, User author) {
        return messageRepo.findAllByAuthor(currentUser, pageable, author);
    }
}
