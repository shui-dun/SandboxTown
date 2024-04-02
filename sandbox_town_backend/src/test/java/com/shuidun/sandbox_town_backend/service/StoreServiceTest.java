package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.bean.StoreItemTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import com.shuidun.sandbox_town_backend.mapper.StoreItemTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @InjectMocks
    private StoreService service;

    @Mock
    private StoreItemTypeMapper storeItemTypeMapper;

    @Mock
    private SpriteService spriteService;

    @Mock
    private SpriteMapper spriteMapper;

    @Mock
    private ItemService itemService;

    // 测试商品存在，数量充足，用户余额足够的情况
    @Test
    public void testBuyItemSuccessfully() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 1;

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10); // 假定库存为10
        mockStoreItemType.setPrice(100); // 假定价格为100

        SpriteDo mockSprite = new SpriteDo();
        mockSprite.setMoney(500); // 假定用户余额为500

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);
        when(spriteService.selectById(spriteId)).thenReturn(mockSprite);

        service.buy(spriteId, store, item, amount);

        // 验证余额是否更新为400
        assertEquals(400, mockSprite.getMoney());
        // 验证商品数量是否更新为9
        assertEquals(9, mockStoreItemType.getCount());
        // 验证是否向用户添加了商品（由于没有返回值，所以只能验证是否调用了方法）
        verify(itemService, times(1)).add(spriteId, item, amount);
        // 验证是否调用了更新用户的方法
        verify(spriteMapper, times(1)).updateById(mockSprite);
        // 验证是否调用了更新商品的方法
        verify(storeItemTypeMapper, times(1)).update(mockStoreItemType);
    }

    // 测试商品不存在
    @Test
    public void testBuyItemNotFoundStatusCode() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 1;

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(null);

        BusinessException thrown = assertThrows(BusinessException.class, () -> service.buy(spriteId, store, item, amount));
        assertSame(thrown.getStatusCode(), StatusCodeEnum.ITEM_NOT_FOUND, "Status code should be ITEM_NOT_FOUND");
    }

    // 测试商品数量不足
    @Test
    public void testBuyItemNotEnoughStatusCode() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 11;

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10);

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);

        BusinessException thrown = assertThrows(BusinessException.class, () -> service.buy(spriteId, store, item, amount));
        assertSame(thrown.getStatusCode(), StatusCodeEnum.ITEM_NOT_ENOUGH, "Status code should be ITEM_NOT_ENOUGH");
    }

    // 测试用户不存在
    @Test
    public void testBuySpriteNotFoundStatusCode() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 1;

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10);

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);
        when(spriteService.selectById(spriteId)).thenReturn(null);

        BusinessException thrown = assertThrows(BusinessException.class, () -> service.buy(spriteId, store, item, amount));
        assertSame(thrown.getStatusCode(), StatusCodeEnum.SPRITE_NOT_FOUND, "Status code should be SPRITE_NOT_FOUND");
    }

    // 测试用户余额不足
    @Test
    public void testBuyMoneyNotEnoughStatusCode() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 3;

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10);
        mockStoreItemType.setPrice(200);

        SpriteDo mockSprite = new SpriteDo();
        mockSprite.setMoney(500);

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);
        when(spriteService.selectById(spriteId)).thenReturn(mockSprite);

        BusinessException thrown = assertThrows(BusinessException.class, () -> service.buy(spriteId, store, item, amount));
        assertSame(thrown.getStatusCode(), StatusCodeEnum.MONEY_NOT_ENOUGH, "Status code should be MONEY_NOT_ENOUGH");
    }

    // 测试商品数量刚好等于请求数量
    @Test
    public void testBuyItemQuantityExactMatch() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 10; // 请求购买的数量与库存相同

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10); // 设置库存数量
        mockStoreItemType.setPrice(100); // 假设价格为100

        SpriteDo mockSprite = new SpriteDo();
        mockSprite.setMoney(1000); // 假设用户余额足以支付

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);
        when(spriteService.selectById(spriteId)).thenReturn(mockSprite);

        service.buy(spriteId, store, item, amount); // 执行购买操作

        // 验证库存更新为0
        assertEquals(0, mockStoreItemType.getCount());
        // 验证用户余额更新
        assertEquals(0, mockSprite.getMoney());
        // 验证是否向用户添加了商品
        verify(itemService, times(1)).add(spriteId, item, amount);
    }

    // 测试用户余额刚好等于商品总价
    @Test
    public void testBuyWhenMoneyEqualsPrice() {
        String spriteId = "spriteId";
        String store = "store";
        ItemTypeEnum item = ItemTypeEnum.APPLE;
        int amount = 5; // 购买数量

        StoreItemTypeDo mockStoreItemType = new StoreItemTypeDo();
        mockStoreItemType.setCount(10); // 库存足够
        mockStoreItemType.setPrice(100); // 商品单价

        SpriteDo mockSprite = new SpriteDo();
        mockSprite.setMoney(500); // 用户余额恰好等于所需支付的总金额

        when(storeItemTypeMapper.selectByStoreAndItemType(store, item)).thenReturn(mockStoreItemType);
        when(spriteService.selectById(spriteId)).thenReturn(mockSprite);

        service.buy(spriteId, store, item, amount); // 执行购买操作

        // 验证用户余额被正确更新为0
        assertEquals(0, mockSprite.getMoney());
        // 验证库存被正确更新
        assertEquals(5, mockStoreItemType.getCount());
        // 验证是否正确向用户添加了商品
        verify(itemService, times(1)).add(spriteId, item, amount);
    }


}
