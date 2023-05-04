package com.nc.nccommunity.controller;

import com.nc.nccommunity.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Date;

@Controller
@RequestMapping("/data")
public class DataController {
	
	@Autowired
	private DataService dataService;
	
	@GetMapping	@PostMapping
	public String getDataPage() {
		return "site/admin/data";
	}
	
	@PostMapping("/uv")
	public String getUV(Model model, @DateTimeFormat(pattern="yyyy-MM-dd")Date begin, @DateTimeFormat(pattern="yyyy-MM-dd")Date end){
		long uv = dataService.calcUV(begin, end);
		model.addAttribute("uvResult", uv);
		model.addAttribute("uvStartDate", begin);
		model.addAttribute("uvEndDate", end);
		
		return "forward:/data";
	}
	
	@PostMapping("/dau")
	public String getDAU(Model model, @DateTimeFormat(pattern="yyyy-MM-dd")Date begin, @DateTimeFormat(pattern="yyyy-MM-dd")Date end){
		long dau = dataService.calcDAU(begin, end);
		model.addAttribute("dauResult", dau);
		model.addAttribute("dauStartDate", begin);
		model.addAttribute("dauEndDate", end);
		
		return "forward:/data";
	}
	
}
