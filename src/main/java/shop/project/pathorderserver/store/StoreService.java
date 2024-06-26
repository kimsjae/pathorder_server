package shop.project.pathorderserver.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.project.pathorderserver._core.errors.exception.App404;
import shop.project.pathorderserver._core.errors.exception.Web401;
import shop.project.pathorderserver._core.errors.exception.Web403;
import shop.project.pathorderserver._core.errors.exception.Web404;
import shop.project.pathorderserver._core.utils.DistanceUtil;
import shop.project.pathorderserver.like.LikeService;
import shop.project.pathorderserver.menu.Menu;
import shop.project.pathorderserver.menu.MenuOption;
import shop.project.pathorderserver.menu.MenuOptionRepository;
import shop.project.pathorderserver.menu.MenuRepository;
import shop.project.pathorderserver.order.*;
import shop.project.pathorderserver.review.ReviewRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final LikeService likeService;
    private final ReviewRepository reviewRepository;

    public int getReviewCount(int storeId) {
        return reviewRepository.findReviewCountByStoreId(storeId);
    }

    // 매장 목록보기
    public List<StoreResponse.StoreListDTO> getStoreList(int userId, double customerLatitude, double customerLongitude) {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(store -> {
                    int likeCount = likeService.getStoreLikeCount(store.getId());
                    boolean isLiked = likeService.isUserLikedStore(userId, store.getId());
                    int reviewCount = getReviewCount(store.getId());
                    int distance = DistanceUtil.calculateDistance(customerLatitude, customerLongitude, store.getLatitude(), store.getLongitude());

                    return new StoreResponse.StoreListDTO(store, likeCount, isLiked, reviewCount, distance);
                })
                .sorted(Comparator.comparingInt(StoreResponse.StoreListDTO::getDistance))
                .toList();
    }

    // 매장 상세보기
    public StoreResponse.StoreInfoDTO getStoreInfo(int userId, int storeId, double customerLatitude, double customerLongitude) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new App404("찾을 수 없는 매장입니다."));

        int likeCount = likeService.getStoreLikeCount(storeId);
        boolean isLiked = likeService.isUserLikedStore(userId, storeId);
        int reviewCount = getReviewCount(storeId);
        int distance = DistanceUtil.calculateDistance(customerLatitude, customerLongitude, store.getLatitude(), store.getLongitude());

        return new StoreResponse.StoreInfoDTO(store, likeCount, isLiked, reviewCount, distance);
    }

    // 매장 상세보기 - 사업자 정보
    public StoreResponse.StoreBizInfoDTO getStoreBizInfo(int storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new App404("찾을 수 없는 매장입니다."));

        return new StoreResponse.StoreBizInfoDTO(store);
    }

    // 매장 메뉴보기
    public StoreResponse.StoreMenuListDTO getStoreMenuList(int storeId) {
        Store store // 매장 정보
                = storeRepository.findById(storeId)
                .orElseThrow(() -> new App404("찾을 수 없는 매장입니다."));
        List<Menu> menus // 매장 메뉴 정보
                = menuRepository.findAllByStoreId(storeId)
                .orElseThrow(() -> new App404("찾을 수 없는 메뉴입니다."));

        return new StoreResponse.StoreMenuListDTO(store, menus);
    }

    // 매장 메뉴 옵션보기
    public StoreResponse.StoreMenuOptionDTO getStoreMenuDetail(int storeId, int menuId) {
        Store store // 매장 정보
                = storeRepository.findById(storeId)
                .orElseThrow(() -> new App404("찾을 수 없는 매장입니다."));
        Menu menu // 매장 메뉴 정보
                = menuRepository.findById(menuId)
                .orElseThrow(() -> new App404("찾을 수 없는 메뉴입니다."));
        List<MenuOption> optionList // 매장 메뉴 옵션 정보
                = menuOptionRepository.findByMenuId(menuId)
                .orElseThrow(() -> new App404("찾을 수 없는 옵션입니다."));

        return new StoreResponse.StoreMenuOptionDTO(store, menu, optionList);
    }

    /*------------------------------------------------------------------------------------- 매장 관리자 -----------------*/
    // 매장 관리자 등록
    @Transactional
    public StoreResponse.JoinDTO createStore(StoreRequest.JoinDTO reqDTO) {
        Store store = new Store(reqDTO);
        storeRepository.save(store);
        return new StoreResponse.JoinDTO(store);
    }

    // 매장 관리자 로그인
    public SessionStore getStore(StoreRequest.LoginDTO reqDTO) {
        Store store = storeRepository.findByUsernameAndPassword(reqDTO.getUsername(), reqDTO.getPassword())
                .orElseThrow(() -> new Web401("유저네임 또는 패스워드가 일치하지 않습니다."));

        return new SessionStore(store);
    }

    // TODO: 매장 관리자 - 매장 정보 보기
    public StoreResponse.StoreDTO getStoreDetail(int storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new Web403("조회할 권한이 없습니다."));

        return new StoreResponse.StoreDTO(store);
    }

    @Transactional // 매장 관리자 - 매장 정보 수정하기
    public SessionStore updateStore(int sessionStoreId, StoreRequest.UpdateDTO reqDTO) {
        Store store = storeRepository.findById(sessionStoreId)
                .orElseThrow(() -> new Web403("수정할 권한이 없습니다."));
        store.update(reqDTO);

        return new SessionStore(store);
    }

    @Transactional // 매장 관리자 - 매장 메뉴 등록하기
    public StoreResponse.CreateMenuDTO createMenu(int storeId, StoreRequest.CreateMenuDTO reqDTO) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new Web404("찾을 수 없는 매장입니다."));
        Menu menu = new Menu(reqDTO, store);

        return new StoreResponse.CreateMenuDTO(menuRepository.save(menu));
    }

    // 매장 관리자 - 메뉴 목록보기
    public StoreResponse.MenuListDTO getMenuList(int storeId) {
        List<Menu> menus = menuRepository.findAllByStoreId(storeId)
                .orElseThrow(() -> new Web404("메뉴를 찾을 수 없습니다."));

        return new StoreResponse.MenuListDTO(menus);
    }

    // 매장 관리자 - 메뉴 정보 및 옵션 보기
    public StoreResponse.MenuDetailDTO getMenuDetail(int menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new Web404("찾을 수 없는 메뉴입니다."));
        List<MenuOption> menuOptions = menuOptionRepository.findByMenuId(menuId)
                .orElse(new ArrayList<>());

        return new StoreResponse.MenuDetailDTO(menu, menuOptions);
    }

    @Transactional // 매장 관리자 - 메뉴 & 메뉴 옵션 수정하기
    public StoreResponse.UpdateMenuDTO updateMenu(int menuId, StoreRequest.UpdateMenuDTO reqDTO) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new Web404("찾을 수 없는 메뉴입니다."));
        menu.update(reqDTO);
        menuOptionRepository.deleteByMenuId(menuId);

        List<MenuOption> menuOptions = new ArrayList<>();
        for (StoreRequest.UpdateMenuDTO.MenuOptionDTO menuOptionDTO : reqDTO.getMenuOptions()) {
            MenuOption menuOption = new MenuOption(menuOptionDTO, menu);
            menuOptionRepository.save(menuOption);
            menuOptions.add(menuOption);
        }

        return new StoreResponse.UpdateMenuDTO(menu, menuOptions);
    }

    @Transactional // 매장 관리자 - 메뉴 삭제하기
    public void deleteMenu(int menuId) {
        menuOptionRepository.deleteByMenuId(menuId);
        menuRepository.deleteById(menuId);
    }

    // 매장 관리자 - 주문내역 목록보기
    public StoreResponse.OrderListDTO getOrderList(int storeId) {
        List<Order> orderList = orderRepository.findAllByStoreId(storeId)
                .orElseThrow(() -> new Web404("주문 내역이 없습니다."));
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PENDING)).toList().forEach(orderList::remove);
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PREPARING)).toList().forEach(orderList::remove);
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PREPARED)).toList().forEach(orderList::remove);

        // 이넘 -> 한글
        orderList.forEach(order -> {
            OrderStatus status = order.getStatus();
        });

        return new StoreResponse.OrderListDTO(orderList);
    }

    @Transactional(readOnly = true)// 매장 관리자 - 주문내역 상세보기
    public StoreResponse.OrderDetailDTO getOrderDetail(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Web404("찾을 수 없는 주문입니다."));
        List<OrderMenu> orderMenuList = orderMenuRepository.findAllByOrderId(orderId)
                .orElseThrow(() -> new Web404("찾을 수 없는 메뉴입니다."));

        return new StoreResponse.OrderDetailDTO(order, orderMenuList);
    }

    @Transactional // TODO: 매장 관리자 - 주문 업데이트
    public StoreResponse.UpdateOrderDTO updateOrder(int orderId, StoreRequest.UpdateOrderDTO reqDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Web404("찾을 수 없는 주문입니다."));
        if (reqDTO.getStatus().equals(OrderStatus.PENDING)) {
            order.setStatus(OrderStatus.PREPARING);
        }
        if (reqDTO.getStatus().equals(OrderStatus.PREPARING)) {
            order.setStatus(OrderStatus.PREPARED);
        }
        if (reqDTO.getStatus().equals(OrderStatus.PREPARED)) {
            order.setStatus(OrderStatus.SERVED);
        }

        return new StoreResponse.UpdateOrderDTO(order);
    }

    public HashMap<String, Object> getOrders(int storeId) {
        // 전체 오더 리스트
        List<Order> orders = orderRepository.findAllByStoreIdWithOrderMenu(storeId)
                .orElseThrow(() -> new Web404("찾을 수 없는 주문입니다."));
        // 응답할 오더 리스트
        List<StoreResponse.OrdersDTO> orderList = new ArrayList<>();
        orders.forEach(order ->
                orderList.add(StoreResponse.OrdersDTO.builder()
                        .order(order)
                        .menuList(order.getOrderMenus())
                        .build())
        );

        List<StoreResponse.OrdersDTO> pendingOrderList = new ArrayList<>();
        List<StoreResponse.OrdersDTO> preparingOrderList = new ArrayList<>();
        List<StoreResponse.OrdersDTO> preparedOrderList = new ArrayList<>();
        for (StoreResponse.OrdersDTO ordersDTO : orderList) {
            if (ordersDTO.getStatus() == OrderStatus.PENDING) {
                pendingOrderList.add(ordersDTO);
            }
            if (ordersDTO.getStatus() == OrderStatus.PREPARING) {
                preparingOrderList.add(ordersDTO);
            }
            if (ordersDTO.getStatus() == OrderStatus.PREPARED) {
                preparedOrderList.add(ordersDTO);
            }
        }

        int pendingOrderCount = pendingOrderList.size();

        HashMap<String, Object> orderListSortedByStatus = new HashMap<>();
        orderListSortedByStatus.put("pendingOrderList", pendingOrderList);
        orderListSortedByStatus.put("preparingOrderList", preparingOrderList);
        orderListSortedByStatus.put("preparedOrderList", preparedOrderList);

        // 접수하지 않은 주문 카운트(주문탭 옆의 숫자)
        orderListSortedByStatus.put("pendingOrderCount", pendingOrderCount);

        return orderListSortedByStatus;
    }

    public StoreResponse.OrderListDTO getOrderListByDate(int storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Order> orderList = orderRepository.findAllByStoreIdAndCreatedAtBetween(storeId, startDateTime, endDateTime);

        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PENDING)).toList().forEach(orderList::remove);
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PREPARING)).toList().forEach(orderList::remove);
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.PREPARED)).toList().forEach(orderList::remove);
        orderList.stream().filter(order -> order.getStatus().equals(OrderStatus.CONFIRMED)).toList().forEach(orderList::remove);

        // 이넘 -> 한글
        orderList.forEach(order -> {
            OrderStatus status = order.getStatus();
        });
        return new StoreResponse.OrderListDTO(orderList);
    }

    public int getPendingOrderCount(int storeId) {
        List<Order> orders = orderRepository.findAllByStoreId(storeId)
                .orElseThrow(() -> new Web404("찾을 수 없는 주문입니다."));
        int pendingOrderCount = 0;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getStatus() == OrderStatus.PENDING) {
                pendingOrderCount++;
            }
        }
        return pendingOrderCount;
    }
}
