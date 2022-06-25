package cn.gzsendi.system.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * 读取yml配置的工具类
 * @author liujh
 */
public class YmlUtils {
	
	public static final String defaultFileName = "application.yml";
	
	/**
     * key:文件名索引
     * value:配置文件内容
     */
	@SuppressWarnings("rawtypes")
	private static Map<String, LinkedHashMap> ymls = new HashMap<>();
    
    /**
     * 加载配置文件
     * @param fileName
     */
    private static void loadYml(String fileName) {
        if (!ymls.containsKey(fileName)) {
        	
        	InputStream in = null;
        	try{
        		in = YmlUtils.class.getResourceAsStream("/" + fileName);
                ymls.put(fileName, new Yaml().loadAs(in, LinkedHashMap.class));
                if(in != null) in.close();
        	}catch (Exception e) {
				throw new RuntimeException("解析yml文件失败,文件不存在或yml文件格式有误.");
			}
        	
        }
    }
    
    //值不存在的话返回null
    @SuppressWarnings("rawtypes")
	public static Object getValue(String fileName, String key) {
        
    	// 首先加载配置文件
        loadYml(fileName);
        
        // 首先将key进行拆分
        String[] keys = key.split("[.]");

        // 将配置文件进行复制
		Map ymlInfo = (Map) ymls.get(fileName).clone();
        for (int i = 0; i < keys.length; i++) {
            Object value = ymlInfo.get(keys[i]);
            if (i < keys.length - 1) {
                ymlInfo = (Map) value;
            } else if (value == null) {
            	
                return null;
                
            } else {
                return value;
            }
        }
        
        return null;
        
    }
    
	public static Object getValue(String fileName, String key, Object defaultValue) {
    	Object result = getValue(fileName, key);
    	return result == null ? defaultValue : result;
    }
    
    public static Object getValueFromDefaultFile(String key) {
    	return getValue(defaultFileName,key);
    }
    
    public static Object getValueFromDefaultFile(String key,Object defaultValue) {
    	Object result = getValue(defaultFileName,key);
    	return result == null ? defaultValue : result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getValueFromDefaultFile("clientPort1", true).getClass());
    }

}
