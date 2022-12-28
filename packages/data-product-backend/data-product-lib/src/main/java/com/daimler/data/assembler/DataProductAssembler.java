/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.assembler;

import java.lang.reflect.Type;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.daimler.data.db.jsonb.AgileReleaseTrain;
import com.daimler.data.db.jsonb.CarLaFunction;
import com.daimler.data.db.jsonb.CorporateDataCatalog;
import com.daimler.data.dto.dataproduct.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.jsonb.CreatedBy;
import com.daimler.data.db.jsonb.dataproduct.DataProduct;
import com.daimler.data.db.jsonb.dataproduct.DataProductClassificationConfidentiality;
import com.daimler.data.db.jsonb.dataproduct.DataProductContactInformation;
import com.daimler.data.db.jsonb.dataproduct.DataProductDeletionRequirement;
import com.daimler.data.db.jsonb.dataproduct.DataProductPersonalRelatedData;
import com.daimler.data.db.jsonb.dataproduct.DataProductTransnationalDataTransfer;
import com.daimler.data.db.jsonb.dataproduct.Division;
import com.daimler.data.db.jsonb.dataproduct.Subdivision;
import com.daimler.data.db.jsonb.dataproduct.TeamMember;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.DataProductTeamMemberVO.UserTypeEnum;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class DataProductAssembler implements GenericAssembler<DataProductVO, DataProductNsql> {

	@Override
	public DataProductVO toVo(DataProductNsql entity) {
		DataProductVO vo = null;
		if (entity != null && entity.getData() != null) {
			vo = new DataProductVO();
			vo.setId(entity.getId());
			DataProduct dataProduct = entity.getData();
			if (dataProduct != null) {
				BeanUtils.copyProperties(dataProduct, vo);
				vo.setIsPublish(entity.getData().getPublish());
				if (Objects.nonNull(dataProduct.getCreatedBy())) {
					CreatedByVO createdByVO = new CreatedByVO();
					BeanUtils.copyProperties(dataProduct.getCreatedBy(), createdByVO);
					vo.setCreatedBy(createdByVO);
				}
				if (Objects.nonNull(dataProduct.getModifiedBy())) {
					CreatedByVO updatedByVO = new CreatedByVO();
					BeanUtils.copyProperties(dataProduct.getModifiedBy(), updatedByVO);
					vo.setModifiedBy(updatedByVO);
				}
				if (Objects.nonNull(dataProduct.getCarLaFunction())) {
					CarLaFunctionVO carLaFunctionVO = new CarLaFunctionVO();
					BeanUtils.copyProperties(dataProduct.getCarLaFunction(), carLaFunctionVO);
					vo.setCarLaFunction(carLaFunctionVO);
				}
				if (Objects.nonNull(dataProduct.getAgileReleaseTrain())) {
					AgileReleaseTrainVO agileReleaseTrain = new AgileReleaseTrainVO();
					BeanUtils.copyProperties(dataProduct.getAgileReleaseTrain(), agileReleaseTrain);
					vo.setAgileReleaseTrain(agileReleaseTrain);
				}
				if (Objects.nonNull(dataProduct.getCorporateDataCatalog())) {
					CorporateDataCatalogVO corporateDataCatalog = new CorporateDataCatalogVO();
					BeanUtils.copyProperties(dataProduct.getCorporateDataCatalog(), corporateDataCatalog);
					vo.setCorporateDataCatalog(corporateDataCatalog);
				}
				
				DataProductContactInformation dataProductContactInformation = dataProduct.getContactInformation();
				if (dataProductContactInformation != null) {
					DataProductContactInformationVO contactInformationVO = new DataProductContactInformationVO();
					BeanUtils.copyProperties(dataProductContactInformation, contactInformationVO);
					DivisionVO divisionvo = new DivisionVO();
					Division division = dataProductContactInformation.getDivision();
					if (division != null) {
						BeanUtils.copyProperties(division, divisionvo);
						SubdivisionVO subdivisionVO = new SubdivisionVO();
						if (division.getSubdivision() != null)
							BeanUtils.copyProperties(division.getSubdivision(), subdivisionVO);
						divisionvo.setSubdivision(subdivisionVO);
						contactInformationVO.setDivision(divisionvo);
					}

					contactInformationVO.setName(toTeamMemberVO(dataProductContactInformation.getName()));
					contactInformationVO.setDataProductDate(dataProductContactInformation.getDataTransferDate());
					contactInformationVO.setInformationOwner(toTeamMemberVO(dataProductContactInformation.getInformationOwner()));
					vo.setContactInformation(contactInformationVO);
				}

				if (dataProduct.getClassificationConfidentiality() != null) {
					DataProductClassificationConfidentialityVO classificationConfidentialityVO = new DataProductClassificationConfidentialityVO();
					BeanUtils.copyProperties(dataProduct.getClassificationConfidentiality(),
							classificationConfidentialityVO);
					vo.setClassificationConfidentiality(classificationConfidentialityVO);
				}

				if (dataProduct.getPersonalRelatedData() != null) {
					DataProductPersonalRelatedDataVO personalRelatedDataVO = new DataProductPersonalRelatedDataVO();
					BeanUtils.copyProperties(dataProduct.getPersonalRelatedData(), personalRelatedDataVO);
					vo.setPersonalRelatedData(personalRelatedDataVO);
				}

				if (dataProduct.getTransnationalDataTransfer() != null) {
					DataProductTransnationalDataTransferVO transnationalDataTransferVO = new DataProductTransnationalDataTransferVO();
					BeanUtils.copyProperties(dataProduct.getTransnationalDataTransfer(), transnationalDataTransferVO);
					vo.setTransnationalDataTransfer(transnationalDataTransferVO);
				}

				if (dataProduct.getDeletionRequirement() != null) {
					DataProductDeletionRequirementVO deletionRequirementVO = new DataProductDeletionRequirementVO();
					BeanUtils.copyProperties(dataProduct.getDeletionRequirement(), deletionRequirementVO);
					vo.setDeletionRequirement(deletionRequirementVO);
				}
				if (dataProduct.getOpenSegments() != null && !ObjectUtils.isEmpty(dataProduct.getOpenSegments())) {
					List<DataProductVO.OpenSegmentsEnum> openSegmentsEnumList = new ArrayList<>();
					dataProduct.getOpenSegments().forEach(openSegment -> openSegmentsEnumList
							.add(DataProductVO.OpenSegmentsEnum.valueOf(openSegment)));
					vo.setOpenSegments(openSegmentsEnumList);
				}
			}
		}
		return vo;
	}

	@Override
	public DataProductNsql toEntity(DataProductVO vo) {
		DataProductNsql entity = null;
		if (vo != null) {
			entity = new DataProductNsql();
			String id = vo.getId();
			if (StringUtils.hasText(id)) {
				entity.setId(id);
			}
			DataProduct dataProduct = new DataProduct();
			
			if (vo != null) {
				BeanUtils.copyProperties(vo, dataProduct);
				dataProduct.setNotifyUsers(vo.isNotifyUsers());
				dataProduct.setPublish(vo.isIsPublish());
				if (Objects.nonNull(vo.getCreatedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(vo.getCreatedBy(), userDetails);
					dataProduct.setCreatedBy(userDetails);
				}
				if (Objects.nonNull(vo.getModifiedBy())) {
					CreatedBy userDetails = new CreatedBy();
					BeanUtils.copyProperties(vo.getModifiedBy(), userDetails);
					dataProduct.setModifiedBy(userDetails);
				}
				if (Objects.nonNull(vo.getCarLaFunction())) {
					CarLaFunction carLaFunction = new CarLaFunction();
					BeanUtils.copyProperties(vo.getCarLaFunction(), carLaFunction);
					dataProduct.setCarLaFunction(carLaFunction);
				}
				if (Objects.nonNull(vo.getAgileReleaseTrain())) {
					AgileReleaseTrain agileReleaseTrain = new AgileReleaseTrain();
					BeanUtils.copyProperties(vo.getAgileReleaseTrain(), agileReleaseTrain);
					dataProduct.setAgileReleaseTrain(agileReleaseTrain);
				}
				if (Objects.nonNull(vo.getCorporateDataCatalog())) {
					CorporateDataCatalog corporateDataCatalog = new CorporateDataCatalog();
					BeanUtils.copyProperties(vo.getCorporateDataCatalog(), corporateDataCatalog);
					dataProduct.setCorporateDataCatalog(corporateDataCatalog);
				}
//				if (!ObjectUtils.isEmpty(vo.getUsers())) {
//					List<TeamMember> users = vo.getUsers().stream().map(n -> toTeamMemberJson(n))
//							.collect(Collectors.toList());
//					dataProduct.setUsers(users);
//				}
				DataProductContactInformationVO dataProductContactInformationVO = vo.getContactInformation();
				if (dataProductContactInformationVO != null) {
					DataProductContactInformation contactInformation = new DataProductContactInformation();
					BeanUtils.copyProperties(dataProductContactInformationVO, contactInformation);
					DivisionVO divisionVO = dataProductContactInformationVO.getDivision();
					if (divisionVO != null) {
						Division division = new Division();
						BeanUtils.copyProperties(divisionVO, division);
						if (divisionVO.getSubdivision() != null) {
							Subdivision subdivision = new Subdivision();
							BeanUtils.copyProperties(divisionVO.getSubdivision(), subdivision);
							division.setSubdivision(subdivision);
						}
						contactInformation.setDivision(division);
					}
					contactInformation.setName(toTeamMemberJson(dataProductContactInformationVO.getName()));
					contactInformation.setDataTransferDate(dataProductContactInformationVO.getDataProductDate());
					contactInformation.setInformationOwner(toTeamMemberJson(dataProductContactInformationVO.getInformationOwner()));
					dataProduct.setContactInformation(contactInformation);
				}

				if (vo.getClassificationConfidentiality() != null) {
					DataProductClassificationConfidentiality classificationConfidentiality = new DataProductClassificationConfidentiality();
					BeanUtils.copyProperties(vo.getClassificationConfidentiality(),
							classificationConfidentiality);
					dataProduct.setClassificationConfidentiality(classificationConfidentiality);
				}

				DataProductPersonalRelatedDataVO personalRelatedDataVO = vo.getPersonalRelatedData();
				if (personalRelatedDataVO != null) {
					DataProductPersonalRelatedData personalRelatedData = new DataProductPersonalRelatedData();
					BeanUtils.copyProperties(personalRelatedDataVO, personalRelatedData);
					personalRelatedData.setPersonalRelatedData(personalRelatedDataVO.isPersonalRelatedData());
					dataProduct.setPersonalRelatedData(personalRelatedData);
				}

				DataProductTransnationalDataTransferVO transnationalDataTransferVO = vo
						.getTransnationalDataTransfer();
				if (transnationalDataTransferVO != null) {
					DataProductTransnationalDataTransfer transnationalDataTransfer = new DataProductTransnationalDataTransfer();
					BeanUtils.copyProperties(transnationalDataTransferVO, transnationalDataTransfer);
					transnationalDataTransfer.setDataTransferred(transnationalDataTransferVO.isDataTransferred());
					transnationalDataTransfer.setNotWithinEU(transnationalDataTransferVO.isNotWithinEU());
					transnationalDataTransfer.setDataFromChina(transnationalDataTransferVO.isDataFromChina());
					dataProduct.setTransnationalDataTransfer(transnationalDataTransfer);
				}

				DataProductDeletionRequirementVO deletionRequirementVO = vo.getDeletionRequirement();
				if (deletionRequirementVO != null) {
					DataProductDeletionRequirement deletionRequirement = new DataProductDeletionRequirement();
					BeanUtils.copyProperties(deletionRequirementVO, deletionRequirement);
					deletionRequirement.setDeletionRequirements(deletionRequirementVO.isDeletionRequirements());
					dataProduct.setDeletionRequirement(deletionRequirement);
				}

				if (!ObjectUtils.isEmpty(vo.getOpenSegments())) {
					List<String> openSegmentList = new ArrayList<>();
					vo.getOpenSegments().forEach(openSegmentsEnum -> {
						openSegmentList.add(openSegmentsEnum.name());
					});
					dataProduct.setOpenSegments(openSegmentList);
				}
			}
			entity.setData(dataProduct);
		}

		return entity;
	}

	private DataProductTeamMemberVO toTeamMemberVO(TeamMember teamMember) {
		DataProductTeamMemberVO vo = null;
		if (teamMember != null) {
			vo = new DataProductTeamMemberVO();
			BeanUtils.copyProperties(teamMember, vo);
			if (StringUtils.hasText(teamMember.getUserType())) {
				vo.setUserType(UserTypeEnum.valueOf(teamMember.getUserType()));
			}
		}
		return vo;
	}

	private TeamMember toTeamMemberJson(DataProductTeamMemberVO vo) {
		TeamMember teamMember = null;
		if (vo != null) {
			teamMember = new TeamMember();
			BeanUtils.copyProperties(vo, teamMember);
			teamMember.setAddedByProvider(vo.isAddedByProvider());
			if (vo.getUserType() != null) {
				teamMember.setUserType(vo.getUserType().name());
			}
		}
		return teamMember;
	}

	/**
	 * Simple GSON based json objects compare and difference provider
	 * 
	 * @param request
	 * @param existing
	 * @param currentUser
	 * @return
	 */
	public List<ChangeLogVO> jsonObjectCompare(Object request, Object existing, CreatedByVO currentUser) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> leftMap = gson.fromJson(gson.toJson(existing), type);
		Map<String, Object> rightMap = gson.fromJson(gson.toJson(request), type);

		Map<String, Object> leftFlatMap = DataProductAssembler.flatten(leftMap);
		Map<String, Object> rightFlatMap = DataProductAssembler.flatten(rightMap);

		MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

		TeamMemberVO teamMemberVO = new TeamMemberVO();
		BeanUtils.copyProperties(currentUser, teamMemberVO);
		teamMemberVO.setShortId(currentUser.getId());
		Date changeDate = new Date();

		List<ChangeLogVO> changeLogsVO = new ArrayList<ChangeLogVO>();
		ChangeLogVO changeLogVO = null;
		// Checking for Removed values
		if (null != difference.entriesOnlyOnLeft() && !difference.entriesOnlyOnLeft().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnLeft().entrySet()) {
				changeLogVO = new ChangeLogVO();
				changeLogVO.setModifiedBy(teamMemberVO);
				changeLogVO.setChangeDate(changeDate);
				changeLogVO.setFieldChanged(entry.getKey());
				changeLogVO.setOldValue(entry.getValue().toString());
				// setting change Description Starts
				changeLogVO
						.setChangeDescription(toChangeDescription(entry.getKey(), entry.getValue().toString(), null));
				changeLogsVO.add(changeLogVO);
			}
		}
		// Checking for Added values
		if (null != difference.entriesOnlyOnRight() && !difference.entriesOnlyOnRight().isEmpty()) {
			for (Entry<String, Object> entry : difference.entriesOnlyOnRight().entrySet()) {
				changeLogVO = new ChangeLogVO();
				changeLogVO.setModifiedBy(teamMemberVO);
				changeLogVO.setChangeDate(changeDate);
				changeLogVO.setFieldChanged(entry.getKey());
				changeLogVO.setNewValue(entry.getValue().toString());
				// setting change Description
				changeLogVO
						.setChangeDescription(toChangeDescription(entry.getKey(), null, entry.getValue().toString()));
				changeLogsVO.add(changeLogVO);

			}
		}
		// Checking for value differences
		if (null != difference.entriesDiffering() && !difference.entriesDiffering().isEmpty()) {
			for (Entry<String, ValueDifference<Object>> entry : difference.entriesDiffering().entrySet()) {
				changeLogVO = new ChangeLogVO();
				changeLogVO.setModifiedBy(teamMemberVO);
				changeLogVO.setChangeDate(changeDate);
				changeLogVO.setFieldChanged(entry.getKey());
				changeLogVO.setOldValue(entry.getValue().leftValue().toString());
				changeLogVO.setNewValue(entry.getValue().rightValue().toString());
				// setting change Description
				changeLogVO.setChangeDescription(toChangeDescription(entry.getKey(),
						entry.getValue().leftValue().toString(), entry.getValue().rightValue().toString()));
				changeLogsVO.add(changeLogVO);
			}
		}
		return changeLogsVO;
	}

	/**
	 * flatten the map
	 * 
	 * @param map
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> flatten(Map<String, Object> map) {
		if (null == map || map.isEmpty()) {
			return new HashMap<String, Object>();
		} else {
			return map.entrySet().stream().flatMap(DataProductAssembler::flatten).collect(LinkedHashMap::new,
					(m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
		}
	}

	/**
	 * flatten map entry
	 * 
	 * @param entry
	 * @return
	 */
	private static Stream<Entry<String, Object>> flatten(Entry<String, Object> entry) {

		if (entry == null) {
			return Stream.empty();
		}

		if (entry.getValue() instanceof Map<?, ?>) {
			Map<?, ?> properties = (Map<?, ?>) entry.getValue();
			return properties.entrySet().stream()
					.flatMap(e -> flatten(new SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
		}

		if (entry.getValue() instanceof List<?>) {
			List<?> list = (List<?>) entry.getValue();
			return IntStream.range(0, list.size())
					.mapToObj(i -> new SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
					.flatMap(DataProductAssembler::flatten);
		}

		return Stream.of(entry);
	}

	private String toHumanReadableFormat(String raw) {
		if (raw != null) {
			String seperated = raw.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
					"(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
			String formatted = Character.toUpperCase(seperated.charAt(0)) + seperated.substring(1);
			return formatted;
		} else
			return raw;
	}

	/**
	 * toChangeDescription convert given keyString to changeDescription
	 * 
	 * @param keyString
	 * @param fromValue
	 * @param toValue
	 * @return changeDescription
	 */
	private String toChangeDescription(String keyString, String fromValue, String toValue) {
		keyString = keyString.substring(1);
		String[] keySet = keyString.split("/");
		String at = null;
		int indexValue = 0;
		StringBuilder changeDescription = new StringBuilder();
		if (keySet.length > 0) {
			String fieldValue = toHumanReadableFormat(keySet[0]);
			changeDescription.append(fieldValue + ": ");
		}
		boolean flag = false;
		for (int i = (keySet.length - 1), index = keySet.length; i >= 0; i--) {
			if (!keySet[i].matches("[0-9]") && !flag) {
				changeDescription.append(toHumanReadableFormat(keySet[i]));
				flag = true;
			} else if (keySet[i].matches("[0-9]")) {
				indexValue = Integer.parseInt(keySet[i]) + 1;
				at = " at index " + String.valueOf(indexValue);
				index = i;
			} else {
				changeDescription.append(" of " + toHumanReadableFormat(keySet[i]));
			}
			if (StringUtils.hasText(at) && index != i) {
				changeDescription.append(at);
				at = null;
			}

		}
		if (!StringUtils.hasText(fromValue)) {
			changeDescription.append(" `" + toValue + "` added . ");
		} else if (!StringUtils.hasText(toValue)) {
			changeDescription.append(" `" + fromValue + "` removed . ");
		} else {
			changeDescription.append(" changed from `" + fromValue + "` to `" + toValue + "` .");
		}

		return changeDescription.toString();
	}

}