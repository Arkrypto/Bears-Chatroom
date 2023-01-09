package com.northboat.bearchat.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


// 待完善
@Component
@SuppressWarnings("all")
public class RedisUtil {

    private RedisTemplate myRedisTemplate;
    @Autowired
    public void setMyRedisTemplate(RedisTemplate myRedisTemplate){
        this.myRedisTemplate = myRedisTemplate;
    }


    //设置有效时间，单位秒
    public boolean expire(String key, long time){
        try{
            if(time > 0){
                myRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //获取剩余有效时间
    public long getExpire(String key){
        return myRedisTemplate.getExpire(key);
    }

    //判断键是否存在
    public boolean hasKey(String key){
        try{
            return myRedisTemplate.hasKey(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //批量删除键
    public void del(String... key){
        if(key != null && key.length > 0){
            if(key.length == 1){
                myRedisTemplate.delete(key[0]);
            } else {
                myRedisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    //获取普通值
    public Object get(String key){
        return key == null ? null : myRedisTemplate.opsForValue().get(key);
    }

    //放入普通值
    public boolean set(String key, Object val){
        try{
            myRedisTemplate.opsForValue().set(key, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //放入普通缓存并设置时间
    public boolean set(String key, Object val, long time){
        try{
            if(time > 0){
                myRedisTemplate.opsForValue().set(key, val, time, TimeUnit.SECONDS);
            } else { // 若时间小于零直接调用普通设置的方法放入
                this.set(key, val);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    //值增
    public long incr(String key, long delta){
        if(delta < 0){
            throw new RuntimeException("递增因子必须大于零");
        }
        return myRedisTemplate.opsForValue().increment(key, delta);
    }




    //============Map=============

    // 获取key表中itme对应的值
    public Object hget(String key, String item){
        return myRedisTemplate.opsForHash().get(key, item);
    }

    // 获取整个Hash表
    public Map hmget(String key){
        return myRedisTemplate.opsForHash().entries(key);
    }

    // 简单设置一个Hash
    public boolean hmset(String key, Map<String, Object> map){
        try{
            myRedisTemplate.opsForHash().putAll(key, map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // 设置一个Hash，并设置生效时间，调用上面的设置key生效时间的方法
    public boolean hmset(String key, Map<String, Object> map, long time){
        try{
            myRedisTemplate.opsForHash().putAll(key, map);
            if(time > 0){
                this.expire(key, time);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }


    // 像一张Hash表中添加键值，若表不存在将创建
    public boolean hset(String key, String item, Object val){
        try{
            myRedisTemplate.opsForHash().put(key, item, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //=================List==============
    public boolean lpush(String key, Object val){
        try{
            myRedisTemplate.opsForList().leftPush(key, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean rpush(String key, Object val){
        try{
            myRedisTemplate.opsForList().rightPush(key, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public List lget(String key){
        try{
            Long length = myRedisTemplate.opsForList().size(key);
            List list = myRedisTemplate.opsForList().range(key, 0, length);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean lldel(String key, String val){
        try{
            myRedisTemplate.opsForList().remove(key, 1, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // 从list最右边开始检索val
    public boolean lrdel(String key, String val){
        try{
            myRedisTemplate.opsForList().remove(key, -1, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //=================Set=================
    // 添加
    public boolean sadd(String key, String val){
        try{
            myRedisTemplate.opsForSet().add(key, val);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    // 删除
    public boolean srem(String key, String val){
        try{
            myRedisTemplate.opsForSet().remove(key, val);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    // 判存
    public boolean sexist(String key, String val){
        try{
            return myRedisTemplate.opsForSet().isMember(key, val);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    // 返回集合
    public Set sget(String key){
        try{
            return myRedisTemplate.opsForSet().members(key);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
