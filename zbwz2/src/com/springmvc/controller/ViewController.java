package com.springmvc.controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;




@Controller
public class ViewController {
	@ResponseBody
	@RequestMapping("/view")
	public String view(HttpServletRequest request){
//		String path = request.getParameter("path") + "";
//		ModelAndView mav = new ModelAndView();
//		
//		String contextPath = request.getContextPath();
//		mav.addObject("contextPath" , contextPath);
//		System.out.println("this is controller--");
//		
//		mav.setViewName(path);
//		return mav;
//		String id=request.getParameter("id");
//		request.getSession().setAttribute("id", id);
		System.out.println(request.getParameter("title"));
//		file.get
		System.out.println("---");
		System.out.println(request.getServletContext().getRealPath("/"));
		return "ok";
	}

	
	@ResponseBody
	@RequestMapping("/upload")
	public void upload(HttpServletRequest request,HttpServletResponse response,MultipartFile image){
		System.out.println("-------");
		System.out.println(image.getSize());
		String imgName=image.getOriginalFilename();
		
		String apachePath=request.getSession().getServletContext().getRealPath("/")+"//resources//img//";
		System.out.println(apachePath);
		File saveImg= new File(apachePath+image.getOriginalFilename());
		try {
			image.transferTo(saveImg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ;
	}
	
	@RequestMapping(value = "/upload33",method = RequestMethod.POST)
    public BasicJson addOwner(HttpServletRequest request,MultipartFile face)
    {	
    	BasicJson basicJson=new BasicJson();
    	System.out.println("before upload");
    	String imagePath=null;
    	//上传人脸
    	try {
    		//获取文件原始名称
    		String originalFilename = face.getOriginalFilename();
    		//上传图片
    		if(face!=null && originalFilename!=null && originalFilename.length()>0){
    			//存储图片的物理路径
    			String pic_path = "D://j2ee_estate//estateOA-master//WebContent//view//img//face//owner//";
    			String apachePath=request.getSession().getServletContext().getRealPath("/")+"//view//img//face//owner//";
    			//新的图片名称
    			String newFileName = UUID.randomUUID()+ originalFilename.substring(originalFilename.lastIndexOf("."));
    			//新图片
    			File newFile= new File(apachePath+newFileName);
    			//将内存中的数据写入磁盘
    			face.transferTo(newFile);
    			HttpSession session = request.getSession();
    			
				/*
				 * IO流复制
				 */
    			try {
    				System.out.println("ready to copy..........");
    				InputStream in=new FileInputStream(apachePath+newFileName);
    				OutputStream out=new FileOutputStream(new File(pic_path+newFileName));
    				byte[] buffer=new byte[1024*10];
    				int len=0;
    				while((len=in.read(buffer))!=-1){
    					out.write(buffer);
    				}
    				in.close();
    				out.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
	
    		}
		} catch (Exception e) {
			// TODO: handle exception
			basicJson.getErrorMsg().setDescription("出错");
		}

    	
        return basicJson;
    }
	
}
