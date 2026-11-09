package com.hytmc.mod.ess.coremod;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name(value="ess")
@IFMLLoadingPlugin.SortingIndex(value=1002)
public class CorePlugin
implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() {
        return new String[]{"com.hytmc.mod.ess.coremod.CoreTransformer"};
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

