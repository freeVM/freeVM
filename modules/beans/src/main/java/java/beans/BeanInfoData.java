/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.beans;

class BeanInfoData {

    private Class<?> stopClass;
    private boolean ignoreBeanClassBeanInfo;
    private boolean ignoreSuperClassBeanInfo;
    private BeanInfoWrapper beanInfoWrapper;

    public BeanInfoData(BeanInfoWrapper beanInfo) {
        this.stopClass = null;
        this.ignoreBeanClassBeanInfo = false;
        this.ignoreSuperClassBeanInfo = false;
        this.beanInfoWrapper = beanInfo;
    }

    public BeanInfoData(Class<?> stopClass, boolean ignoreBeanClassBeanInfo,
        boolean ignoreSuperClassBeanInfo, BeanInfoWrapper beanInfoWrapper)
    {
        this.stopClass = stopClass;
        this.ignoreBeanClassBeanInfo = ignoreBeanClassBeanInfo;
        this.ignoreSuperClassBeanInfo = ignoreSuperClassBeanInfo;
        this.beanInfoWrapper = beanInfoWrapper;
    }

    public Class<?> getStopClass() {
        return stopClass;
    }

    public boolean getIgnoreBeanClassBeanInfo() {
        return ignoreBeanClassBeanInfo;
    }

    public boolean getIgnoreSuperClassBeanInfo() {
        return ignoreSuperClassBeanInfo;
    }

    public BeanInfoWrapper getBeanInfoWrapper() {
        return beanInfoWrapper;
    }
}
