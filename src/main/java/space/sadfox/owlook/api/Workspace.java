
package space.sadfox.owlook.api;

import space.sadfox.owlook.ui.base.Controller;

public interface Workspace {
  Controller getController();

  String getWorkspaceName();

  String getWorkspaceDescriprion();

}
