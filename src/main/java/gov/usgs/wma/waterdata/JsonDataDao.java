package gov.usgs.wma.waterdata;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

@Component
public class JsonDataDao {
	private static final Logger LOG = LoggerFactory.getLogger(JsonDataDao.class);

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/approvals.sql")
	private Resource approvals;

	@Value("classpath:sql/description.sql")
	private Resource description;

	@Value("classpath:sql/gapTolerances.sql")
	private Resource gapTolerances;

	@Value("classpath:sql/grades.sql")
	private Resource grades;

	@Value("classpath:sql/headerInfo.sql")
	private Resource headerInfo;

	@Value("classpath:sql/interpolationTypes.sql")
	private Resource interpolationTypes;

	@Value("classpath:sql/methods.sql")
	private Resource methods;

	@Value("classpath:sql/points.sql")
	private Resource points;

	@Value("classpath:sql/qualifiers.sql")
	private Resource qualifiers;

	@Transactional
	public void doApprovals(Long jsonDataId) {
		doUpdate(jsonDataId, approvals);
	}

	@Transactional
	public void doGapTolerances(Long jsonDataId) {
		doUpdate(jsonDataId, gapTolerances);
	}

	@Transactional
	public void doGrades(Long jsonDataId) {
		doUpdate(jsonDataId, grades);
	}

	@Transactional
	public String doHeaderInfo(Long jsonDataId) {
		try {
			return jdbcTemplate.queryForObject(
					getSql(headerInfo),
					String.class,
					jsonDataId
				);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("Couldn't find {} - {}", jsonDataId, e.getLocalizedMessage());
			//Eat the no data exception if the JSON data is not found or
			//has no time series unique ID. We cannot recover from this and
			//should not trigger a state machine retry.
			return null;
		}
	}

	@Transactional
	public void doInterpolationTypes(Long jsonDataId) {
		doUpdate(jsonDataId, interpolationTypes);
	}

	@Transactional
	public void doMethods(Long jsonDataId) {
		doUpdate(jsonDataId, methods);
	}

	@Transactional
	public void doPoints(Long jsonDataId) {
		doUpdate(jsonDataId, points);
	}

	@Transactional
	public void doQualifiers(Long jsonDataId) {
		doUpdate(jsonDataId, qualifiers);
	}

	@Transactional
	public TimeSeries getRouting(String timeSeriesUniqueId) {
		try {
			return jdbcTemplate.queryForObject(
					getSql(description),
					new TimeSeriesRowMapper(),
					timeSeriesUniqueId
				);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("Couldn't find {} - {}", timeSeriesUniqueId, e.getLocalizedMessage());
			//Eat the no data exception these are rows we do not want to process.
			return null;
		}
	}

	@Transactional
	protected void doUpdate(Long jsonDataId, Resource resource) {
		jdbcTemplate.update(getSql(resource), jsonDataId);
	}

	protected String getSql(Resource resource) {
		String sql = null;
		try {
			sql = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
		return sql;
	}
}
