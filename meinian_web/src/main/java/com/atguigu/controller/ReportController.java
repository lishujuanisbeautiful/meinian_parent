package com.atguigu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.constant.MessageConstant;
import com.atguigu.entity.Result;
import com.atguigu.service.MemberService;
import com.atguigu.service.ReportService;
import com.atguigu.service.SetmealService;
import com.atguigu.util.DateUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: meinian_parent
 * @description:
 * @author: lwl
 * @create: 2022-06-09 23:38
 */
@RestController
@RequestMapping("/report")
public class ReportController {
    @Reference
    MemberService memberService;

    @Reference
    SetmealService setmealService;

    @Reference
    ReportService reportService;

    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport(){

        try {
            List<String>   setmealNames= new ArrayList<>();
            List<Map>  setmealCount=setmealService.getSetmealReport();

            for (Map map : setmealCount) {
                setmealNames.add((String) map.get("name"));
            }
            Map map=new HashMap<>();
            map.put("setmealNames", setmealNames);
            map.put("setmealCount", setmealCount);

            return  new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }

    }


    @RequestMapping("/getBusinessReportData")
    public  Result getBusinessReportData(){
        try {
            Map<String,Object> map=reportService.getBusinessReportData();
            return  new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS, map);
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    @RequestMapping("/exportBusinessReport")
    public  void exportBusinessReport(HttpServletRequest request,HttpServletResponse response){
        try {
            //1.?????????
            Map<String,Object> result=reportService.getBusinessReportData();
            //?????????????????????????????????????????????????????????Excel?????????
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");
            //2.??????????????????
            String filepath=request.getSession().getServletContext().getRealPath("template")+ File.separator +"report_template.xlsx";
            //3.?????????
            Workbook workbook=new XSSFWorkbook(new File(filepath));
            //???????????????
            Sheet sheet = workbook.getSheetAt(0);

            //4.?????????
            Row row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);
            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);//???????????????????????????
            row.getCell(7).setCellValue(totalMember);//????????????

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);//?????????????????????
            row.getCell(7).setCellValue(thisMonthNewMember);//?????????????????????

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);//???????????????
            row.getCell(7).setCellValue(todayVisitsNumber);//???????????????

            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);//???????????????
            row.getCell(7).setCellValue(thisWeekVisitsNumber);//???????????????

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);//???????????????
            row.getCell(7).setCellValue(thisMonthVisitsNumber);//???????????????


            int rowNum=12;
            for (Map map : hotSetmeal) {//????????????
                String name= (String) map.get("name");
                Long setmeal_count=(Long)map.get("setmeal_count");
                BigDecimal proportion=(BigDecimal)map.get("proportion");
                row = sheet.getRow(rowNum++);
                row.getCell(4).setCellValue(name);
                row.getCell(5).setCellValue(setmeal_count);
                row.getCell(6).setCellValue(proportion.doubleValue());
            }
            //5.????????????,???????????????,????????????,???????????????
            ServletOutputStream out = response.getOutputStream();
            // ????????????????????????excel?????????
            response.setContentType("application/vnd.ms-excel");
            // ??????????????????(???????????????????????????)
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");
            workbook.write(out);

            //6.?????????
            out.flush();
            out.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                request.getRequestDispatcher("/pages/error/downerror.html").forward(request, response);
            } catch (ServletException servletException) {
                servletException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }



//    ???????????? ???  ????????????
    @RequestMapping("/getMemberReport")
    public  Result getMemberReport(){
        //??????????????????
        Calendar calendar=Calendar.getInstance();
        //????????????  1.???????????? 2.??????????????????????????????????????????
        //???????????????????????????12??????????????????
        calendar.add(Calendar.MONTH, -12);
        List<String> list=new ArrayList<>();
        for (int i=0;i<12;i++){
            calendar.add(Calendar.MONTH, 1);
            list.add(new SimpleDateFormat("yyyy-MM").format(calendar.getTime()));
        }
        Map<String,Object> map=new HashMap<>();
        map.put("months", list);
        List<Integer> memberCount=memberService.findMemberCountByMonth(list);
        map.put("memberCount", memberCount);
        return  new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS, map);
    }
}