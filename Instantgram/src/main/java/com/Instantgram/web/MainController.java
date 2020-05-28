package com.Instantgram.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.Instantgram.model.Post;
import com.Instantgram.model.User;
import com.Instantgram.repository.PostRepository;
import com.Instantgram.service.StorageService;
import com.Instantgram.service.UserService;
import com.Instantgram.web.dto.FileResponse;

@Controller
public class MainController {

	@Autowired
	UserService userService;

	@Autowired
	PostRepository postRepository;

	@Autowired
	StorageService storageService;

	@GetMapping("/")
	public String root() {
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String home() {
		return "wip";
	}

	@GetMapping("/login")
	public String login(Model model) {
		return "auth/login";
	}

	@GetMapping("/profile/{uname}")
	public String profile(@PathVariable("uname") String uname, Model model) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = "";

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		if (uname.equals(username)) {
			model.addAttribute("isSelf", "true");
		}
		User currentUser = userService.findByUsername(uname);
		System.out.println(currentUser);
		model.addAttribute("username", uname);
		model.addAttribute("fullName", currentUser.getFullName());
		return "profile";
	}

	@PostMapping("/upload")
	@ResponseBody
	public FileResponse upload(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
		System.out.println("uploaded");
		String name = storageService.store(file);

		String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/pictures/").path(name).toUriString();

		String username = authentication.getName();

		User user = userService.findByUsername(username);

		Post post = new Post(uri, user);

		postRepository.save(post);

		return new FileResponse(name, uri, file.getContentType(), file.getSize());
	}

	@GetMapping("/pictures/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws FileNotFoundException {

		Resource resource = storageService.loadAsResource(filename);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping("/posts/{uname}")
	@ResponseBody
	public Set<Post> getPosts(@PathVariable String uname, Authentication authentication) {

		System.out.println(uname);

		 User currentUser = userService.findByUsername(uname);
		 //List<Post> list = postRepository.findAll();
		 Set<Post> posts = currentUser.getPosts();
		 
		return posts;

	}
	
	@GetMapping("/getusers")
	@ResponseBody
	public Set<User> getUsers(@RequestParam String subStrUser) {
		return userService.findByUsernameIgnoreCaseContainingOrFullNameIgnoreCaseContaining(subStrUser, subStrUser);
	}
	
	@DeleteMapping("posts/{postid}")
	@ResponseBody
	public String deletePost(@PathVariable Long postid, Authentication authentication) throws IOException {
		postRepository.deleteById(postid);
		return "Post deleted";
	}

	@GetMapping("/nav")
	public String nav(Model model) {
		String username = "";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		String fullName = userService.findByUsername(username).getFullName();
		model.addAttribute("username", username);
		model.addAttribute("fullName", fullName);

		return "nav";
	}
}
