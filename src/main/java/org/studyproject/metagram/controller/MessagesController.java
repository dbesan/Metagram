package org.studyproject.metagram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.studyproject.metagram.domain.Message;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.domain.dto.MessageDto;
import org.studyproject.metagram.repos.MessageRepo;
import org.studyproject.metagram.service.MessageService;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.studyproject.metagram.config.Literals.NOTHING_TO_CHANGE;

@Controller
public class MessagesController {

    private final MessageRepo messageRepo;
    private MessageService messageService;

    @Autowired
    public MessagesController(MessageRepo messageRepo, MessageService messageService) {
        this.messageRepo = messageRepo;
        this.messageService = messageService;
    }

    /**
     * @GetMapping("/main") String main (@RequestParam(required = false, defaultValue = "") String filter, Model model,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
     * @AuthenticationPrincipal User user) - get all page, with all messages and pagination
     */
    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter, Model model,
                       @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                       @AuthenticationPrincipal User user) {
        Page<MessageDto> page = messageService.messageList(filter, pageable, user);
        model.addAttribute("pageCount", page.getTotalPages());
        model.addAttribute("pageNumber", page.getNumber());
        model.addAttribute("page", page);
        model.addAttribute("filter", filter);
        model.addAttribute("user", user);
        return "main";
    }


    /**
     * @PostMapping("/main") String add(@RequestParam("file") MultipartFile file,
     * @AuthenticationPrincipal User user,
     * Model model,
     * @Valid Message message,
     * BindingResult bindingResult,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
     * - adding message from main page form. return main page.
     */
    @PostMapping("/main")
    public String add(@RequestParam("file") MultipartFile file,
                      @AuthenticationPrincipal User user,
                      Model model,
                      @Valid Message message,
                      BindingResult bindingResult,
                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) throws IOException {
        messageService.addMessage(file, user, model, message, bindingResult, pageable);

        return "main";
    }

    /**
     * @GetMapping("/user-messages/{author}") String userMessages(
     * @AuthenticationPrincipal User currentUser,
     * @PathVariable User author,
     * Model model,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
     * - get messages from specified author
     */
    @GetMapping("/user-messages/{author}")
    public String userMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User author,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<MessageDto> page = messageService.messageListForUser(pageable, currentUser, author);
        model.addAttribute("user", author);
        model.addAttribute("page", page);
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("pageCount", page.getTotalPages());
        return "userMessages";
    }

    /**
     * @PostMapping("/user-messages/{id}/add") public String add(@RequestParam("file") MultipartFile file,
     * @PathVariable Long id,
     * @AuthenticationPrincipal User user,
     * Model model,
     * @Valid Message message,
     * BindingResult bindingResult,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
     * - add message from authorized people channel page.
     */
    @PostMapping("/user-messages/{id}/add")
    public String add(@RequestParam("file") MultipartFile file,
                      @PathVariable Long id,
                      @AuthenticationPrincipal User user,
                      Model model,
                      @Valid Message message,
                      BindingResult bindingResult,
                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) throws IOException {
        messageService.addMessage(file, user, model, message, bindingResult, pageable);

        return "redirect:/user-messages/" + id;
    }


    /**
     * @GetMapping("/user-messages/{author}/message={message}") String updateMessageGet(
     * @AuthenticationPrincipal User currentUser,
     * @PathVariable User author,
     * Model model,
     * @PathVariable Message message,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
     * - updation message form.
     */

    @GetMapping("/user-messages/{author}/message={message}")
    public String updateMessageGet(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User author,
            Model model,
            @PathVariable Message message,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MessageDto> page = messageRepo.findAllByAuthor(currentUser, pageable, author);
        model.addAttribute("isEditing", true);
        model.addAttribute("user", author);
        model.addAttribute("page", page);
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("pageCount", page.getTotalPages());
        return "userMessages";
    }

    /**
     * PostMapping("/user-messages/{user}")
     * public String updateMessage(@AuthenticationPrincipal User currentUser,
     *
     * @PathVariable Long user,
     * @RequestParam("id") Message message,
     * @RequestParam("text") String text,
     * @RequestParam("tag") String tag,
     * @RequestParam("file") MultipartFile file,
     * Model model)
     * Saving updated message.
     */
    @PostMapping("/user-messages/{user}")
    public String updateMessage(@AuthenticationPrincipal User currentUser,
                                @PathVariable Long user,
                                @RequestParam("id") Message message,
                                @RequestParam("text") String text,
                                @RequestParam("tag") String tag,
                                @RequestParam("file") MultipartFile file,
                                Model model
    ) throws IOException, NullPointerException {

        try {
            if (message.getAuthor().equals(currentUser)) {

                if (!StringUtils.isEmpty(text)) {
                    message.setText(text);
                }
                if (!StringUtils.isEmpty(tag)) {
                    message.setTag(tag);
                }
                messageService.saveFile(file, message);
                messageRepo.save(message);
            }
        } catch (NullPointerException e) {
            model.addAttribute("idError", NOTHING_TO_CHANGE);
        }

        return "redirect:/user-messages/" + user;
    }

    /**
     * @GetMapping("/user-messages/{author}/delete={delete}") public String deleteUserMessages(
     * @AuthenticationPrincipal User currentUser,
     * @PathVariable User author,
     * Model model,
     * @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, @PathVariable Long delete)
     * -delete message
     */
    @GetMapping("/user-messages/{author}/delete={delete}")
    public String deleteUserMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User author,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, @PathVariable Long delete) {
        Message message = messageRepo.findById(delete).get();
        File file = new File("/uploads/" + message.getFilename());
        file.delete();
        messageRepo.delete(message);
        Page<MessageDto> page = messageRepo.findAllByAuthor(currentUser, pageable, author);

        model.addAttribute("user", author);
        model.addAttribute("page", page);
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("pageCount", page.getTotalPages());

        return "userMessages";
    }

    /**
     * @GetMapping("/messages/{message}/like") public String like(@AuthenticationPrincipal User currentUser,
     * @PathVariable Message message,
     * RedirectAttributes redirectAttributes,
     * @RequestHeader(required = false) String referer)
     * like/unlike message
     */
    @GetMapping("/messages/{message}/like")
    public String like(@AuthenticationPrincipal User currentUser,
                       @PathVariable Message message,
                       RedirectAttributes redirectAttributes,
                       @RequestHeader(required = false) String referer) {
        Set<User> likes = message.getLikes();
        if (likes.contains(currentUser)) {
            likes.remove(currentUser);
        } else {
            likes.add(currentUser);
        }
        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();

        components
                .getQueryParams()
                .entrySet()
                .forEach(pair -> redirectAttributes
                        .addAttribute(pair.getKey(), pair.getValue()));
        return "redirect:" + components.getPath();
    }


}
