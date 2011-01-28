/*
* Copyright 2008-2010 Nokia Siemens Networks Oyj
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package hudson.plugins.robot.model;

import hudson.model.AbstractBuild;
import hudson.model.AbstractModelObject;
import hudson.plugins.robot.RobotBuildAction;

import org.apache.commons.lang.StringUtils;

public abstract class RobotTestObject extends AbstractModelObject{
	

	private RobotBuildAction parentAction;

	public RobotBuildAction getParentAction() {
		return parentAction;
	}

	public void setParentAction(RobotBuildAction parentAction) {
		this.parentAction = parentAction;
	}

	public abstract String getName();
	
	public abstract  RobotTestObject getParent();
	
	
	/**
	 * Generates the full packagename
	 * @return
	 */
	public String getRelativePackageName(RobotTestObject thisObject) {
		StringBuilder sb = new StringBuilder(getName());

		RobotTestObject parent = getParent();
		if(parent != null && !parent.equals(thisObject)){
			String parentPackage = parent.getRelativePackageName(thisObject);
			if(StringUtils.isNotBlank(parentPackage)){
				sb.insert(0, ".");
				sb.insert(0, parentPackage);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Generates the url to suite from packagename
	 * @return
	 */
	public String getRelativeId(RobotTestObject thisObject){
		StringBuilder sb = new StringBuilder(safe(getName()));

		RobotTestObject parent = getParent();
		if(parent != null && !parent.equals(thisObject)){
			String parentId = parent.getRelativeId(thisObject);
			if(StringUtils.isNotBlank(parentId)){
				sb.insert(0, "/");
				sb.insert(0, parentId);
			}
		}
		return sb.toString();
	}
	
	public AbstractBuild<?,?> getOwner(){
		return getParentAction().getBuild();
	}
	
	/**
	 * Get the corresponding result object from previous build
	 * @return
	 */
	public RobotTestObject getPreviousResult() {
        AbstractBuild<?,?> build = getOwner();
        if (build == null) {
            return null;
        }
        while(true) {
            build = build.getPreviousBuild();
            if(build == null)
                return null;
            RobotBuildAction parentAction = build.getAction(getParentAction().getClass());
            if(parentAction != null) {
                RobotTestObject result = parentAction.findObjectById(getRelativeId(getParentAction().getResult()));
                if (result!=null)
                    return result;
            }
        }
    }
	
	/**
	 * Get the corresponding result object from a given build
	 * @param build
	 * @return
	 */
	public RobotTestObject getResultInBuild(AbstractBuild<?,?> build) {
        parentAction = build.getAction(RobotBuildAction.class);
        String id = getRelativeId(getParentAction().getResult());
        return (parentAction == null) ? null : parentAction.findObjectById(id);
    }

	public static String safe(String unsafeName) {
		return unsafeName.replace("/", "_").replace("\\", "_")
		.replace(":", "_").replace(" ", "_");
	}
	
	/**
	 * Get URL-safe name for the object
	 * @return
	 */
	public String getSafeName(){
		return safe(getName());
	}
	
}