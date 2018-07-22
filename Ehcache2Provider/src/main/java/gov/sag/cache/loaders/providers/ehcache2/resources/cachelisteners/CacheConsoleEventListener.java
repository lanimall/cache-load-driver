package gov.sag.cache.loaders.providers.ehcache2.resources.cachelisteners;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;


public class CacheConsoleEventListener implements CacheEventListener, Cloneable {

    @Override
    public void notifyElementRemoved(Ehcache paramEhcache, Element paramElement)
            throws CacheException {
        // TODO Auto-generated method stub
        if (paramElement != null && paramElement.getKey() != null) {
            System.out.println(paramElement.getKey() + " was removed");
        }
    }

    @Override
    public void notifyElementPut(Ehcache paramEhcache, Element paramElement)
            throws CacheException {
        // TODO Auto-generated method stub
        if (paramElement != null && paramElement.getKey() != null) {
            System.out.println("Listener " + paramElement.getKey() + " was put");
        }
    }

    @Override
    public void notifyElementUpdated(Ehcache paramEhcache, Element paramElement)
            throws CacheException {
        // TODO Auto-generated method stub
        if (paramElement != null && paramElement.getKey() != null) {
            System.out.println(paramElement.getKey() + " was updated");
        }
    }

    @Override
    public void notifyElementExpired(Ehcache paramEhcache, Element paramElement) {
        // TODO Auto-generated method stub
        if (paramElement != null && paramElement.getKey() != null) {
            System.out.println(paramElement.getKey() + " was expired");
        }
    }

    @Override
    public void notifyElementEvicted(Ehcache paramEhcache, Element paramElement) {
        // TODO Auto-generated method stub
        if (paramElement != null && paramElement.getKey() != null) {
            System.out.println(paramElement.getKey() + " was evicted");
        }
    }

    @Override
    public void notifyRemoveAll(Ehcache paramEhcache) {
        // TODO Auto-generated method stub
        System.out.println("Remove All from EventListener!");

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

}
