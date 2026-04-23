package com.github.argon4w.acceleratedrendering.features.mods;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;

import java.util.ArrayDeque;
import java.util.Deque;

public class ModsFeature {

	private static final Deque<FeatureStatus> EMF_ACCELERATION_CONTROLLER_STACK		= new ArrayDeque<>();
	private static final Deque<FeatureStatus> GECKO_ACCELERATION_CONTROLLER_STACK	= new ArrayDeque<>();
	private static final Deque<FeatureStatus> TLM_ACCELERATION_CONTROLLER_STACK		= new ArrayDeque<>();
	private static final Deque<FeatureStatus> SBM_ACCELERATION_CONTROLLER_STACK		= new ArrayDeque<>();
	private static final Deque<FeatureStatus> FTB_ACCELERATION_CONTROLLER_STACK		= new ArrayDeque<>();

	public static boolean isEnabled() {
		return FeatureConfig.CONFIG.modsFeatureStatus.get() == FeatureStatus.ENABLED;
	}

	public static boolean shouldAccelerateEmf() {
		return getEmfSetting() == FeatureStatus.ENABLED;
	}

	public static boolean shouldAccelerateGecko() {
		return getGeckoSetting() == FeatureStatus.ENABLED;
	}

	public static boolean shouldAccelerateTlm() {
		return getTlmSetting() == FeatureStatus.ENABLED;
	}

	public static boolean shouldAccelerateSbm() {
		return getSbmSetting() == FeatureStatus.ENABLED;
	}

	public static boolean shouldAccelerateFtb() {
		return getFtbSetting() == FeatureStatus.ENABLED;
	}

	public static void disableEmfAcceleration() {
		EMF_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableEmfAcceleration() {
		EMF_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetEmfAcceleration(FeatureStatus status) {
		EMF_ACCELERATION_CONTROLLER_STACK.push(status);
	}

	public static void resetEmfAcceleration() {
		EMF_ACCELERATION_CONTROLLER_STACK.pop();
	}

	public static void disableGeckoAcceleration() {
		GECKO_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableGeckoAcceleration() {
		GECKO_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetGeckoAcceleration(FeatureStatus status) {
		GECKO_ACCELERATION_CONTROLLER_STACK.push(status);
	}

	public static void resetGeckoAcceleration() {
		GECKO_ACCELERATION_CONTROLLER_STACK.pop();
	}

	public static void disableTlmAcceleration() {
		TLM_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableTlmAcceleration() {
		TLM_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetTlmAcceleration(FeatureStatus status) {
		TLM_ACCELERATION_CONTROLLER_STACK.push(status);
	}

	public static void resetTlmAcceleration() {
		TLM_ACCELERATION_CONTROLLER_STACK.pop();
	}

	public static void disableSbmAcceleration() {
		SBM_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableSbmAcceleration() {
		SBM_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetSbmAcceleration(FeatureStatus status) {
		SBM_ACCELERATION_CONTROLLER_STACK.push(status);
	}

	public static void resetSbmAcceleration() {
		SBM_ACCELERATION_CONTROLLER_STACK.pop();
	}

	public static void disableFtbAcceleration() {
		FTB_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableFtbAcceleration() {
		FTB_ACCELERATION_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetFtbAcceleration(FeatureStatus status) {
		FTB_ACCELERATION_CONTROLLER_STACK.push(status);
	}

	public static void resetFtbAcceleration() {
		FTB_ACCELERATION_CONTROLLER_STACK.pop();
	}

	public static FeatureStatus getEmfSetting() {
		return EMF_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultEmfSetting() : EMF_ACCELERATION_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getGeckoSetting() {
		return GECKO_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultGeckoSetting() : GECKO_ACCELERATION_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getTlmSetting() {
		return TLM_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultTlmSetting() : TLM_ACCELERATION_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getSbmSetting() {
		return SBM_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultSbmSetting() : SBM_ACCELERATION_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getFtbSetting() {
		return FTB_ACCELERATION_CONTROLLER_STACK.isEmpty() ? getDefaultFtbSetting() : FTB_ACCELERATION_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getDefaultEmfSetting() {
		return FeatureConfig.CONFIG.modsEmfFeatureStatus.get();
	}

	public static FeatureStatus getDefaultGeckoSetting() {
		return FeatureConfig.CONFIG.modsGeckoFeatureStatus.get();
	}

	public static FeatureStatus getDefaultTlmSetting() {
		return FeatureConfig.CONFIG.modsTlmFeatureStatus.get();
	}

	public static FeatureStatus getDefaultSbmSetting() {
		return FeatureConfig.CONFIG.modsSbmFeatureStatus.get();
	}

	public static FeatureStatus getDefaultFtbSetting() {
		return FeatureConfig.CONFIG.modsFtbFeatureStatus.get();
	}
}
