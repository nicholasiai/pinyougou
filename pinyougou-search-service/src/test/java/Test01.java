import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;

public class Test01 {

	@Test
	public void test01() {
		Goods goods = new Goods();
		TbGoods tbGoods = new TbGoods();
		tbGoods.setAuditStatus("1");
		
		goods.setGoods(tbGoods);
		
		
		System.out.println(goods.getGoods().getAuditStatus());
		
		TbGoods goods2 = goods.getGoods();
		
		goods2.setAuditStatus("2");
		
		System.out.println(goods.getGoods().getAuditStatus());
		
		System.out.println(goods.getGoods()==goods2);
		
	}
	
	@Test
	public void test02() {
		String random = RandomStringUtils.random(6);
		String randomNumeric = RandomStringUtils.randomNumeric(6);
		System.out.println(randomNumeric);
		
		
	}
	
	
}
