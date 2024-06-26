package shop.project.pathorderserver.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import shop.project.pathorderserver.MyRestDoc;
import shop.project.pathorderserver._core.utils.JwtUtil;
import shop.project.pathorderserver.user.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ReviewControllerTest extends MyRestDoc {

    private ObjectMapper om = new ObjectMapper();
    private static String jwt;

    @BeforeAll
    public static void setUp() {
        jwt = JwtUtil.create(
                User.builder()
                        .id(1)
                        .username("user1")
                        .nickname("성재")
                        .build()
        );
    }

    // 리뷰 등록 성공
    @Test
    public void add_review_success_test() throws Exception {
        //given
        int storeId = 1;
        ReviewRequest.AddDTO reqDTO = new ReviewRequest.AddDTO();
        reqDTO.setContent("맛있었어요");
        reqDTO.setEncodedData(null);
        String reqBody = om.writeValueAsString(reqDTO);
        System.out.println("reqBody : " + reqBody);
        //when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/stores/" + storeId + "/reviews")
                        .header("Authorization", "Bearer " + jwt)
                        .content(reqBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        //then
        actions.andExpect(jsonPath("$.status").value(200));
        actions.andExpect(jsonPath("$.msg").value("성공"));
        actions.andExpect(jsonPath("$.body.content").value("맛있었어요"));
        actions.andExpect(jsonPath("$.body.encodedData").isEmpty());
        actions.andDo(MockMvcResultHandlers.print()).andDo(document);
    }

    // 리뷰 등록 실패 - 공백
    @Test
    public void add_review_content_blank_fail_test() throws Exception {
        //given
        int storeId = 1;
        ReviewRequest.AddDTO reqDTO = new ReviewRequest.AddDTO();

        String reqBody = om.writeValueAsString(reqDTO);
        System.out.println("reqBody : " + reqBody);

        //when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/stores/" + storeId + "/reviews")
                        .header("Authorization", "Bearer " + jwt)
                        .content(reqBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(jsonPath("$.status").value(400));
        actions.andExpect(jsonPath("$.msg").value("내용을 입력해주세요."));
        actions.andExpect(jsonPath("$.body").isEmpty());
        actions.andDo(MockMvcResultHandlers.print()).andDo(document);
    }

    // 리뷰 등록 실패 - 5자 미만
    @Test
    public void add_review_content_size_fail_test() throws Exception {
        //given
        int storeId = 1;
        ReviewRequest.AddDTO reqDTO = new ReviewRequest.AddDTO();
        reqDTO.setContent("1234");
        String reqBody = om.writeValueAsString(reqDTO);
        System.out.println("reqBody : " + reqBody);
        //when
        ResultActions actions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/stores/" + storeId + "/reviews")
                        .header("Authorization", "Bearer " + jwt)
                        .content(reqBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        //then
        actions.andExpect(jsonPath("$.status").value(400));
        actions.andExpect(jsonPath("$.msg").value("내용은 5자 이상 입력해주세요."));
        actions.andExpect(jsonPath("$.body").isEmpty());
        actions.andDo(MockMvcResultHandlers.print()).andDo(document);
    }

    // 내 리뷰 보기
//    @Test
//    public void my_review_list_test() throws Exception {
//        //given
//        int userId = 1;
//        //when
//        ResultActions actions = mockMvc.perform(
//                get("/api/users/" + userId + "/reviews")
//                        .header("Authorization", "Bearer " + jwt)
//        );
//        //then
//        actions.andExpect(jsonPath("$.status").value(200));
//        actions.andExpect(jsonPath("$.msg").value("성공"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].userId").value(1));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].nickname").value("성재"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].usersImgFilePath").value("/upload/default/avatar.png"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].reviewId").value(1));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].content").value("맛있어요"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].imgFilePath").isEmpty());
//        // actions.andExpect(jsonPath("$.body.reviewList.[0].createdAt").value("24/05/21")); 날짜 매번 터짐.
//        actions.andDo(MockMvcResultHandlers.print()).andDo(document);
//    }
//
//    // 매장 리뷰 보기
//    @Test
//    public void store_review_list_test() throws Exception {
//        //given
//        int storeId = 1;
//        //when
//        ResultActions actions = mockMvc.perform(
//                get("/api/stores/" + storeId + "/reviews")
//                        .header("Authorization", "Bearer " + jwt)
//        );
//        //then
//        actions.andExpect(jsonPath("$.status").value(200));
//        actions.andExpect(jsonPath("$.msg").value("성공"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].userId").value(1));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].nickname").value("성재"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].usersImgFilePath").value("/upload/default/avatar.png"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].reviewId").value(1));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].content").value("맛있어요"));
//        actions.andExpect(jsonPath("$.body.reviewList.[0].reviewsImgFilePath").isEmpty());
//        // actions.andExpect(jsonPath("$.body.reviewList.[0].createdAt").value("24/05/20")); 날짜 터짐
//        actions.andDo(MockMvcResultHandlers.print()).andDo(document);
//    }
}
